package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;

@SuppressWarnings("serial")
public class LoginCallbackServlet extends HttpServlet {
	static {
		ObjectifyService.register(User.class);
	}
	public static boolean isProduction = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session;
		if (req.isRequestedSessionIdValid()) {
			session = req.getSession(false); // we should not be creating a new session!
		} else {
			resp.sendError(500, "Session invalid");
			return;
		}
		
		// get query params
    	String error = req.getParameter("error");
    	String code = req.getParameter("code");
    	String stateParam = req.getParameter("state");
    	
    	// calculate the CSRF token
    	String csrfToken = Utils.getCsrfTokenFromSessionId(session.getId());
    	
    	// make sure the query param matches the stored state (CSRF protection)
    	if (stateParam == null || !stateParam.equals(csrfToken)) {
    		resp.sendError(400, "Invalid state");
    		log.log(Level.INFO, "Invalid state", stateParam);
    		return;
    	} else if (error != null) { // check if there's an error
    		resp.sendError(531, "Authentication attempt unsuccessful");
    		log.log(Level.INFO, "Authentication attempt unsuccessful", error);
    		return;
    	} else if (code == null) { // check to make sure the authcode exists
    		resp.sendError(400, "Invalid query parameters");
    		log.log(Level.INFO, "Invalid query parameters");
    		return;
    	}
    	
    	// Try to get an access token using the authcode.
    	String access_token = null;
    	try {
    		access_token = GoogleOAuthClient.getAccessToken(code, GoogleOAuthClient.getRequestHost(req));
    	} catch (Exception e) {
    		resp.sendError(502, "Retrieving access token from Google failed: " + e);
    		log.log(Level.WARNING, "Retrieving access token from Google failed", e);
    		return;
    	}
    	
    	session.setAttribute("access_token", access_token);
    	
    	// Try to get user data from Google Apps.
    	// userData is *not* the user object from the database! It's simply used to store the data.
    	User userData = null;
    	try {
    		userData = GoogleOAuthClient.getUserData(access_token);
    	} catch (Exception e) {
    		resp.sendError(502, "Retrieving user data from Google failed: " + e);
    		log.log(Level.WARNING, "Retrieving user data from Google failed", e);
    		return;
    	}
        
        // log log log
    	log.log(Level.INFO, "Logged in user " + userData.fcps_id);
    	
    	// Get the user from the datastore
    	Ref<User> userRef = ofy().load().type(User.class).filter("fcps_id", userData.fcps_id).first();
    	// Ref<User> userRef = ofy().load().type(User.class).id(userData.fcps_id);
    	User user = userRef.get();
    	
    	// user does not exist in the datastore
    	if (user == null) {
    		// add user, with needs approval
    		user = new User();
    		user.name = userData.name;
    		user.real_name = userData.real_name;
    		user.id = userData.fcps_id;
    		user.fcps_id = userData.fcps_id;
    		user.gender = userData.gender;
    		
    		// for now we'll let it slide
    		// user.accountState = User.AccountState.NEEDS_APPROVAL;
    		ofy().save().entity(user).now();
    		
    		// email meee
    		try {
				Utils.sendEmail(
						"user-approval-notify@edab-ds.appspotmail.com",
						"gengkev@gmail.com", 
						"User approval notification: " + user.name,
						"Name: " + userData.name + "\n" + "Student ID: " + userData.fcps_id
				);
			} catch (MessagingException e) {
				log.log(Level.WARNING, "Error notifying of user approval", user);
			}
    	}
		
		// store the id in the session
		session.setAttribute("userId", userData.fcps_id);
		
		// if we stored closeWindow, then send a callback to the opener, and close the window. Otherwise, send a redirect.
		Object closeWindow = session.getAttribute("closeWindow");
		if (closeWindow != null && ((Boolean) closeWindow)) {
			session.removeAttribute("closeWindow");
			
			PrintWriter writer = resp.getWriter();
			writer.println("<script>try { window.opener.loginCallback(); } catch(e) { window.opener.console.error(e); } window.close();</script>");
			writer.println("You may now <a href=\"#\" onclick=\"window.close()\">close this window</a>.");
			writer.close();
		} else {
			resp.sendRedirect("/");
		}
    }
}