package com.desklampstudios.edab;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@SuppressWarnings("serial")
public abstract class eDABException extends WebApplicationException {
	protected static final String errorName = "GenericError";
	protected static final int statusCode = 400;
	
	protected eDABException(int statusCode, String errorName, String msg) {
		super(Response.status(statusCode)
				.entity(Utils.JsonPad + 
						"{\"error\": {\"name\": \"" + errorName + "\", \"message\": \"" + msg.replace("\"", "\\\"") + "\"}}")
				.type("application/json")
				.build());
	}


	// Implementing classes

	/**
	 * The session ID provided was invalid.
	 * An effort was made to initialize a new session. Please retry.
	 */
	public static class InvalidSessionException extends eDABException {
		private static final String errorName = "InvalidSession";
		private static final int statusCode = 403;
		public InvalidSessionException(String msg) { super(statusCode, errorName, msg); }
	}

	/**
	 * The user is not logged in.
	 */
	public static class NotLoggedInException extends eDABException {
		private static final String errorName = "NotLoggedIn";
		private static final int statusCode = 403;
		public NotLoggedInException(String msg) { super(statusCode, errorName, msg); }
	}

	/**
	 * The user is not authorized to access the requested resource.
	 */
	public static class NotAuthorizedException extends eDABException {
		private static final String errorName = "NotAuthorized";
		private static final int statusCode = 403;
		public NotAuthorizedException(String msg) { super(statusCode, errorName, msg); }
	}

	/**
	 * The request is invalid.
	 */
	public static class InvalidRequestException extends eDABException {
		private static final String errorName = "InvalidRequest";
		private static final int statusCode = 400;
		public InvalidRequestException(String msg) { super(statusCode, errorName, msg); }
	}
	
	/**
	 * Something went wrong internally.
	 */
	public static class InternalServerException extends eDABException {
		private static final String errorName = "InternalServerError";
		private static final int statusCode = 500;
		public InternalServerException(String msg) { super(statusCode, errorName, msg); }
	}
	
	/**
	 * The user's account needs to be manually approved.
	 */
	public static class NeedsApprovalException extends eDABException {
		private static final String errorName = "NeedsApproval";
		private static final int statusCode = 403;
		public NeedsApprovalException(String msg) { super(statusCode, errorName, msg); }
	}
	
	/**
	 * The requested resource was not found.
	 */
	public static class NotFoundException extends eDABException {
		private static final String errorName = "NotFound";
		private static final int statusCode = 404;
		public NotFoundException(String msg) { super(statusCode, errorName, msg); }
	}
}