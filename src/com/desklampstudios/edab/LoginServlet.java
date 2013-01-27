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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession(true); // create one if one does not exist
		String sessionId = session.getId();
		
		// Overriding default session cookie to use HttpOnly, and Secure if we're on https
		String cookie = "JSESSIONID=" + sessionId + "; HttpOnly; Path=/; max-age=" + (60 * 60 * 24 * 7);
		if (req.getScheme() == "https") {
			cookie += "; Secure";
		}
		resp.setHeader("Set-Cookie", cookie);
		
		// If we get passed close, save closeWindow to close the login window when we're done.
		if (req.getParameter("close") != null) {
			session.setAttribute("closeWindow", true);
		}
		
		// Generate a nonce. Gets 8 bytes of random bits from SecureRandom, 
		// and converts it into hexadecimal to put into a StringBuilder.
		SecureRandom rand = new SecureRandom();
		byte[] bytes = new byte[8];
		rand.nextBytes(bytes);
		
		StringBuilder nonce = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString((int) (b + 128)); // shifts [-128, 127] to [0, 255]
			if (hex.length() < 2) {
				nonce.append("0");
			}
			assert hex.length() == 2;
			nonce.append(hex);
		}
		
		// Get the string and set it as a session attribute to be verified later.
		String state = nonce.toString();
		session.setAttribute("state", nonce.toString());
		
		// For some reason, response.sendRedirect appends ;JSESSIONID=BLA
		// which is obviously not accepted by Google. So instead, let's
		// send our own 302 and Location header!
		resp.setStatus(302);
		resp.setHeader("Location", GoogleOAuthClient.getEndpointURL(
				GoogleOAuthClient.getRequestHost(req),
				state));
	}
}
