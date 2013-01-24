package com.desklampstudios.edab;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(true); // create one if one does not exist
		
		String sessionId = session.getId();
		
		// session stuff; gotta make it httponly, let's go
		String cookie = "JSESSIONID=" + sessionId + "; " + /* "HttpOnly;" + */ "Path=/; max-age=" + (60 * 60 * 24 * 7);
		if (req.getScheme() == "https") {
			cookie += "; Secure";
		}
		resp.setHeader("Set-Cookie", cookie);
		
		// yay nonce
		SecureRandom rand = new SecureRandom();
		byte[] bytes = new byte[8];
		rand.nextBytes(bytes);
		
		StringBuilder nonce = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString((int) (b + 128)); // shift [-128, 127] to [0, 255]
			if (hex.length() < 2) {
				nonce.append("0");
			}
			nonce.append(hex);
		}
		
		String state = nonce.toString();
		
		if (req.getParameter("close") != null) {
			state += "|close";
		}

		session.setAttribute("state", state);
		
		// response.sendRedirect is appending jsessionid, wtf?
		// so here's a silly workaround.
		resp.setStatus(302);
		resp.setHeader("Location", GoogleOAuthClient.getEndpointURL(
				GoogleOAuthClient.getRequestHost(req),
				state));
	}
}
