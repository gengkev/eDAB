package com.desklampstudios.edab;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URLEncoder;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginCallbackServlet extends HttpServlet {
	public static boolean isProduction = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String error = req.getParameter("error");
    	String code = req.getParameter("code");
    	
    	if (error != null && !error.isEmpty()) {
    		resp.sendError(531, "Authentication attempt unsuccessful");
    		log.log(Level.INFO, "Authentication attempt unsuccessful", error);
    		return;
    	} else if (code == null || code.isEmpty()) {
    		resp.sendError(400, "Invalid query parameters");
    		log.log(Level.INFO, "Invalid query parameters");
    	}
    	
    	String access_token = null;
    	try {
    		access_token = GoogleOAuthClient.getAccessToken(code, GoogleOAuthClient.getRequestHost(req));
    	} catch (Exception e) {
    		resp.sendError(502, "Retrieving access token from Google failed");
    		log.log(Level.WARNING, "Retrieving access token from Google failed", e);
    		return;
    	}
    	
    	String[] userData = null;
    	try {
    		userData = GoogleOAuthClient.getUserData(access_token);
    	} catch (Exception e) {
    		resp.sendError(502, "Retrieving user data from Google failed");
    		log.log(Level.WARNING, "Retrieving user data from Google failed", e);
    		return;
    	}
    	
    	String name = userData[0],
    			userId = userData[1].replaceFirst("@fcpsschools.net$", "");
        
        // print stuff to the output
    	/*
    	resp.setContentType("text/plain");
    	PrintWriter writer = resp.getWriter();
    	writer.println("Auth code: " + code);
    	writer.println("Access token: " + access_token);
    	writer.println("Email address: " + userId + "@fcpsschools.net");
    	writer.println("Name: " + name);
    	*/
    	
    	
    	// let's store the user as a DB entry.
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    	
    	Query q = new Query("User")
    					.addFilter("userId",
    							Query.FilterOperator.EQUAL,
    							userId);
    	
    	Entity user = datastore.prepare(q).asSingleEntity();
    	
    	if (user == null) { // create an entity!
        	log.log(Level.INFO, "Adding user (name: " + name + ", userId: " + userId + ")");
        	
    		user = new Entity("User", userId); // but don't rely on userId as a key id
    		user.setProperty("name", name);
    		user.setProperty("userId", userId);
    		user.setProperty("created", new Date());
    		datastore.put(user);
    	}
    	
    	// get the session; if one doesn't exist, create one
		HttpSession session = req.getSession(true);
		
		// store the user's email for auth
		session.setAttribute("userId", userId);
		
		// send the session id as HttpOnly
		// we can't use the cookie API or the standard session config cuz of GAE ><
		// String sessionid = session.getId();
		// resp.setHeader("Set-Cookie", "JSESSIONID=" + sessionid + "; HttpOnly; Secure; Path=/; Expires=" + expiryDate);
		
		// Send the email and name as a cookie for the client.
		// Who cares what the client does with it.
		Cookie c = new Cookie("userInfo", URLEncoder.encode(userId, "UTF-8") + ":" + URLEncoder.encode(name, "UTF-8"));
		c.setMaxAge(60 * 60 * 24 * 7);
		c.setPath("/");
		if (req.getScheme() == "https") {
			c.setSecure(true);
		}
		resp.addCookie(c);
		
		// so, how about we actually do something with the response
		resp.sendRedirect("/");
    }
}