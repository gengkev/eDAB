package com.desklampstudios.edab;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URLEncoder;

import com.google.appengine.api.utils.SystemProperty;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginCallbackServlet extends HttpServlet {
	static {
		ObjectifyService.register(User.class);
	}
	public static boolean isProduction = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(false); // we should not be creating one right now!
		
    	String error = req.getParameter("error");
    	String code = req.getParameter("code");
    	String stateParam = req.getParameter("state");
    	
    	String state = (String) session.getAttribute("state");
    	session.removeAttribute("state");
    	
    	if (state == null || !state.equals(stateParam)) {
    		resp.sendError(400, "Invalid state");
    		log.log(Level.INFO, "Invalid state", state);
    		return;
    	} else if (error != null && !error.isEmpty()) {
    		resp.sendError(531, "Authentication attempt unsuccessful");
    		log.log(Level.INFO, "Authentication attempt unsuccessful", error);
    		return;
    	} else if (code == null || code.isEmpty()) {
    		resp.sendError(400, "Invalid query parameters");
    		log.log(Level.INFO, "Invalid query parameters");
    		return;
    	}
    	
    	String access_token = null;
    	try {
    		access_token = GoogleOAuthClient.getAccessToken(code, GoogleOAuthClient.getRequestHost(req));
    	} catch (Exception e) {
    		resp.sendError(502, "Retrieving access token from Google failed: " + e);
    		log.log(Level.WARNING, "Retrieving access token from Google failed", e);
    		return;
    	}
    	
    	String[] userData = null;
    	try {
    		userData = GoogleOAuthClient.getUserData(access_token);
    	} catch (Exception e) {
    		resp.sendError(502, "Retrieving user data from Google failed: " + e);
    		log.log(Level.WARNING, "Retrieving user data from Google failed", e);
    		return;
    	}
    	
    	String name = userData[0], userId = userData[1];
        
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
    	
    	// find the user first
    	Ref<User> userRef = ofy().load().type(User.class).filter("fcps_id", userId).first();
    	User user = userRef.getValue(); // Ref.getValue() returns null if not found
    	
    	if (user == null) { // woops.
    		user = new User();
    		user.name = name;
    		user.fcps_id = userId;
    		ofy().save().entity(user).now();
    		
    		/*
        	log.log(Level.INFO, "Adding user (name: " + name + ", userId: " + userId + ")");
        	
    		user = new Entity("User", userId); // but don't rely on userId as a key id
    		user.setProperty("name", name);
    		user.setProperty("userId", userId);
    		user.setProperty("created", new Date());
    		datastore.put(user);
    		*/
    	}
		
		// store the reference in the session
		session.setAttribute("userId", userId);
		
		// send the session id as HttpOnly
		// we can't use the cookie API or the standard session config cuz of GAE ><
		// String sessionid = session.getId();
		// resp.setHeader("Set-Cookie", "JSESSIONID=" + sessionid + "; HttpOnly; Secure; Path=/; Expires=" + expiryDate);
		
		// so, how about we actually do something with the response
		if (state.indexOf("|close") != -1) {
			PrintWriter writer = resp.getWriter();
			writer.println("<script>try { window.opener.loginCallback(); } catch(e) { alert(e); } window.close();</script>");
			writer.println("You may now <a href=\"#\" onclick=\"window.close()\">close this window</a>.");
			writer.close();
		} else {
			resp.sendRedirect("/");
		}
    }
}