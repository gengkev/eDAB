package com.desklampstudios.edab;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(LogoutServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession(false);
		String access_token = (String) session.getAttribute("access_token");
		
		Cookie cookie = new Cookie("JSESSIONID", "");
		cookie.setMaxAge(-1);
		resp.addCookie(cookie);
		
		session.invalidate();
		
		if (access_token != null) {
			try {
				Utils.fetchURL("GET", "https://accounts.google.com/o/oauth2/revoke?token=" + access_token);
			} catch (IOException e) {
				log.log(Level.WARNING, "Exception trying to revoke token", e);
			}
			
			// don't care about the response
		}
	}
}
