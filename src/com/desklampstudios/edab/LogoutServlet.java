package com.desklampstudios.edab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession(false);
		String access_token = (String) session.getAttribute("access_token");
		
		Cookie cookie = new Cookie("JSESSIONID", "");
		cookie.setMaxAge(-1);
		resp.addCookie(cookie);
		
		session.invalidate();
		
		if (access_token != null) {
			HttpURLConnection connection = null;
			
			/*
			try {
				URL url = new URL("https://accounts.google.com/o/oauth2/revoke?token=" + access_token);
		        connection = (HttpURLConnection) url.openConnection();
		        connection.setRequestMethod("GET");
		        connection.connect();
		        
		        Class[] classes = {String.class};
		        String blah = (String) connection.getContent(classes);
		        log.log(Level.INFO, blah);
			} catch (IOException e) {
				// don't care about the response
				log.log(Level.WARNING, "Exception trying to revoke token", e);
			} finally {
				connection.disconnect();
			}
			*/
			
			try {
				GoogleOAuthClient.fetchURL("GET", "https://accounts.google.com/o/oauth2/revoke?token=" + access_token);
			} catch (IOException e) {
				log.log(Level.WARNING, "Exception trying to revoke token", e);
			}
			
			// don't care about the response
		}
	}
}
