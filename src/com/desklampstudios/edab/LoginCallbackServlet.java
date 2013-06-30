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

@SuppressWarnings("serial")
public class LoginCallbackServlet extends HttpServlet {
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

		// make sure the query param matches the correct CSRF token
		if (stateParam == null || !stateParam.equals(csrfToken)) {
			resp.sendError(400, "Invalid CSRF token");
			log.log(Level.INFO, "Invalid CSRF token", stateParam);
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

		// Try to get user data from Google Apps.
		// userData is *not* the user object from the database! It's simply used for passing the data here.
		User userData = null;
		try {
			userData = GoogleOAuthClient.getUserData(access_token);
		} catch (Exception e) {
			resp.sendError(502, "Retrieving user data from Google failed: " + e);
			log.log(Level.WARNING, "Retrieving user data from Google failed", e);
			return;
		}


		// store the access token from quite a bit back for revoking
		session.setAttribute("access_token", access_token);

		// store the id in the session
		session.setAttribute("userId", userData.id);


		// log log log
		log.log(Level.INFO, "Logged in user id " + userData.id + " w/ username " + userData.fcps_id);


		// Get the user from the datastore
		User user = ofy().load().type(User.class).id(userData.id).get();

		// user does not exist in the datastore
		if (user == null) {
			// try to query for fcps ID
			/*
			user = ofy().load().type(User.class).filter("fcps_id", userData.fcps_id).first().get();
			 */

			// Still null?
			if (user == null) {
				// add user to datastore, with Needs Approval
				user = userData;

				// nope not now
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
		}

		// Screw with imported accounts later.

		// Rotate  session ID on login - to prevent certain attacks (session fixation?)
		session = AccountService.rotateSession(req, resp);
		

		// if we stored closeWindow, then send a callback to the opener, and close the window. Otherwise, send a redirect.
		Boolean closeWindow = (Boolean) session.getAttribute("closeWindow");
		session.removeAttribute("closeWindow");
		
		if (closeWindow != null && closeWindow == true) {
			PrintWriter writer = resp.getWriter();
			writer.println("<script>try { window.opener.loginCallback(); } catch(e) { window.opener.console.error(e); } window.close();</script>");
			writer.println("You may now <a href=\"#\" onclick=\"window.close()\">close this window</a>.");
			writer.close();
		} else {
			resp.sendRedirect("/");
		}
	}
}
