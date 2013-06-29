package com.desklampstudios.edab;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.desklampstudios.edab.eDABException.InvalidSessionException;
import com.desklampstudios.edab.eDABException.NotLoggedInException;


public class AccountService {

	/**
	 * Attempts to use request information to determine the user ID of the current user attempting to access a resource.
	 * 
	 * If the session or CSRF token provided in the request are invalid, the session will become invalidated,
	 * and a new session will be initialized and sent to the client.
	 * Then, an InvalidSessionException will be thrown. The client should attempt to retry with the new session info.
	 * 
	 * If the session is valid, but the user does not have an associated ID, a NotLoggedInException will be thrown.
	 * This does not actually indicate an error state, rather simply that the user has not yet logged in.
	 * 
	 * @param req  The HttpServletRequest object used to identify the user
	 * @param resp The HttpServletResponse object only used when an InvalidSessionException is thrown.
	 * @return currentUserID The ID of the current user making the request.
	 * @throws eDABException
	 */
	static String checkLogin(HttpServletRequest req, HttpServletResponse resp) 
			throws InvalidSessionException, NotLoggedInException {

		// Do we have a valid session ID?
		if (!req.isRequestedSessionIdValid()) {
			// Initialize a new session and send an error response.
			AccountService.initializeSession(req, resp);
			throw new eDABException.InvalidSessionException("Invalid Session ID cookie");
		}

		HttpSession session = req.getSession(false);
		assert session != null;

		// Is the CSRF token valid?
		String providedCSRF = req.getHeader("X-XSRF-Token");
		if (!checkCSRFToken(session.getId(), providedCSRF)) {
			// Invalidate the session, initialize a new session, and send an error response.
			AccountService.initializeSession(req, resp);
			throw new eDABException.InvalidRequestException("Invalid CSRF token in header");
		}

		String currentUserId = (String) session.getAttribute("userId");

		if (currentUserId == null) {
			// Send an error response.
			throw new eDABException.NotLoggedInException("");
		}

		return currentUserId;
	}

	static boolean checkCSRFToken(String sessionId, String providedToken) {
		if (providedToken == null || providedToken.isEmpty()) {
			return false;
		}
		String token = Utils.getCsrfTokenFromSessionId(sessionId);
		return token.equals(providedToken);
	}

	/**
	 * Attempts to initialize a new session and related CSRF token.
	 * If a session already exists, invalidate it first.
	 * 
	 * @param req
	 * @param resp
	 * @return The new session
	 */
	static HttpSession initializeSession(HttpServletRequest req, HttpServletResponse resp) {		
		HttpSession session = req.getSession(false); // attempts to get session
		if (session != null) {
			session.invalidate();
		}

		session = req.getSession(true); // creates new session
		session.setMaxInactiveInterval(Utils.sessionTimeout); // Set the server session timeout

		// Overriding default session cookie to use HttpOnly, and Secure if we're on https
		resp.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; HttpOnly; Path=/; max-age=" + Utils.sessionTimeout);

		// Generate a CSRF token and send it as a cookie - no HttpOnly as it must be js-readable.
		String nonce = Utils.getCsrfTokenFromSessionId(session.getId());
		resp.addHeader("Set-Cookie", "XSRF-TOKEN=" + nonce + "; Path=/; max-age=" + Utils.sessionTimeout);

		return session;
	}

	/**
	 * Attempts to transfer data from one session to another.
	 * Use when changing authentication levels to prevent session fixation.
	 * 
	 * @param req
	 * @param resp
	 * @return The new session
	 */
	static HttpSession rotateSession(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession(false);

		if (session == null) {
			return initializeSession(req, resp);
		}

		// dump all values into HashMap
		HashMap<String, Object> map = new HashMap<String, Object>();

		@SuppressWarnings("unchecked") // the docs say so okay
		Enumeration<String> e = session.getAttributeNames();

		while (e.hasMoreElements()) {
			String name = e.nextElement();
			map.put(name, session.getAttribute(name));
		}

		// invalidate session and get a new one
		session = initializeSession(req, resp);

		// dump all values back into new session
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			session.setAttribute(entry.getKey(), entry.getValue());
		}

		return session;		
	}
}
