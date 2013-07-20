package com.desklampstudios.edab;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(LogoutServlet.class.getName());

	public void revokeAccessToken(String access_token) {
		if (access_token != null) {
			try {
				Utils.fetchURL("POST", "https://accounts.google.com/o/oauth2/revoke?token=" + access_token);
			} catch (IOException e) {
				log.log(Level.WARNING, "Exception trying to revoke token", e);
			}

			// don't care about the response
			// i mean it's not like we could do anything
			// anyway this is just a best-effort thing
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession(false);
		String sessionId = session.getId();
		String userId = (String) session.getAttribute("userId");
		
		// revoke access token
		String access_token = (String) session.getAttribute("access_token");
		revokeAccessToken(access_token);
		
		// Invalidates old session and creates new one
		AccountService.initializeSession(req, resp);
		
		// log log log

		log.log(Level.FINE, "Logged out, sessid: " + sessionId + ", userid: " + userId);
	}
}
