package com.desklampstudios.edab;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@SuppressWarnings("unused")
public abstract class eDABException extends WebApplicationException {
	protected static final long serialVersionUID = 1L;
	protected static final String errorName = "GenericError";
	protected static final int statusCode = 400;

	public eDABException() {
		super(Response.status(statusCode)
				.entity("{\"error\": { \"name\": " + errorName + "\" } }")
				.type("application/json")
				.build());
	}


	// Implementing classes

	/**
	 * The session ID or CSRF token provided was invalid.
	 * An effort was made to initialize a new session. Please retry.
	 */
	public static class InvalidSessionException extends eDABException {
		private static final long serialVersionUID = 1L;
		private static final String errorName = "InvalidSesssion";
		private static final int statusCode = 400;
	}

	/**
	 * The user is not logged in.
	 */
	public static class NotLoggedInException extends eDABException {
		private static final long serialVersionUID = 1L;
		private static final String errorName = "NotLoggedIn";
		private static final int statusCode = 401;
	}

	/**
	 * The user is not authorized to access the requested resource.
	 */
	public static class NotAuthorizedException extends eDABException {
		private static final long serialVersionUID = 1L;
		private static final String errorName = "NotAuthorized";
		private static final int statusCode = 403;
	}

	/**
	 * The request is invalid.
	 */
	public static class InvalidRequestException extends eDABException {
		private static final long serialVersionUID = 1L;
		private static final String errorName = "InvalidRequest";
		private static final int statusCode = 400;
	}
}