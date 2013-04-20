package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.ObjectifyService;

@SuppressWarnings("serial")
public class ServiceServlet3 extends HttpServlet {
	static {
		ObjectifyService.register(User.class);
		ObjectifyService.register(Course.class);
		ObjectifyService.register(School.class);
		ObjectifyService.register(Entry.class);
	}
	private static final Logger log = Logger.getLogger(ServiceServlet3.class.getName());
	
	enum ServiceError {
		SESSION_INIT   ("The session state is being initialized. Please try again."),
		INVALID_CSRF   ("The provided CSRF token was invalid."),
		NOT_LOGGED_IN  ("The user is not logged in."),
		NOT_AUTHORIZED ("The user does not have permission to access the item requested."),
		INVALID_PARAMS ("The request contained invalid parameters.");
		
		private final String msg;
		private ServiceError(String msg) {
			this.msg = msg;
		}
		public String message() {
			return this.msg;
		}
	}
	
	private ServiceService serviceService;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Assume it's for session init.
		if (!request.isRequestedSessionIdValid()) {
			initializeSession(request, response);
		}
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session;
		String csrfToken;
		OutputStream output = response.getOutputStream();
		if (request.isRequestedSessionIdValid()) {
			// we should be all set.
			session = request.getSession(false);
			csrfToken = Utils.getCsrfTokenFromSessionId(session.getId());
		} else {
			// We need to initialize the session.
			initializeSession(request, response);
			
			// Send response. We will not accept this request because we have not verified CSRF.
			Utils.writeOutputStream(output, generateErrorResponse(ServiceError.SESSION_INIT));
			output.close();
			return;
		}
		
		// Check if the header matches the nonce.
		// Screw the cookie.
		String csrfHeader = request.getHeader("X-XSRF-Token");
		if (csrfHeader == null || !csrfHeader.equals(csrfToken)) {
			Utils.writeOutputStream(output, generateErrorResponse(ServiceError.INVALID_CSRF));
			output.close();
			return;
		}
		
		String currentUserId = (String) session.getAttribute("userId");
		if (currentUserId == null) {
			Utils.writeOutputStream(output, generateErrorResponse(ServiceError.NOT_LOGGED_IN));
			output.close();
			return;
		}
		
		
		// otherwise... handle the request
		
		String action = request.getParameter("action"); // ik it's like not hip anymore but too bad
		String json = Utils.readInputStream(request.getInputStream()); // yay
		ObjectMapper mapper = new ObjectMapper();
		
		if (action.equals("getUser")) {
			@SuppressWarnings("unchecked")
			Map<String, Object> jsonData = mapper.readValue(json, Map.class);
			Object idObj = jsonData.get("userId");
			if (!(idObj instanceof String) || idObj == null) {
				Utils.writeOutputStream(output, generateErrorResponse(ServiceError.INVALID_PARAMS));
				output.close();
				return;
			}
			String id = (String) idObj;
			
			User user = ofy().load().group(User.LoadCourses.class).type(User.class).filter("fcps_id", id).first().get();
			
			
		}
	}
	
	protected String generateErrorResponse(ServiceError err) {
		return "{\"error\":{\"code\":\"" + err.toString() + "\",\"message\":\"" + err.message() + "\"}}";
	}
	
	protected void initializeSession(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true); // Creates a new session.
		session.setMaxInactiveInterval(Utils.sessionTimeout); // Set the timeout
		
		// Overriding default session cookie to use HttpOnly, and Secure if we're on https
		response.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; HttpOnly; Path=/; max-age=" + Utils.sessionTimeout);
		
		// Generate a CSRF token and send it as a cookie - no HttpOnly as it must be js-readable.
		String nonce = Utils.getCsrfTokenFromSessionId(session.getId());
		response.addHeader("Set-Cookie", "XSRF-TOKEN=" + nonce + "; Path=/; max-age=" + Utils.sessionTimeout);
	}
}