package com.desklampstudios.edab;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class ServiceServlet3 extends HttpServlet {
	private static final Logger log = Logger.getLogger(ServiceServlet3.class.getName());
	
	private ServiceService serviceService;
	private ModifiedJsonRpcServer jsonRpcServer;
	
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
		String nonce;
		OutputStream output = response.getOutputStream();
		if (request.isRequestedSessionIdValid()) {
			// we should be all set.
			session = request.getSession(false);
			//nonce = (String) session.getAttribute("nonce");
			nonce = Utils.getNonceFromSessionId(session.getId());
		} else {
			// We need to initialize the session.
			initializeSession(request, response);
			
			// Send response. We will not accept this request because we have not verified CSRF.
			Utils.writeOutputStream(output, "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{" + 
					"\"code\":-32600,\"message\":\"Initializing session, please retry.\"}}");
			output.close();
			return;
		}
		
		// Check if the header matches the nonce.
		// Screw the cookie.
		String csrfHeader = request.getHeader("X-XSRF-Token");
		if (!csrfHeader.equals(nonce)) {
			Utils.writeOutputStream(output, "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{" +
					"\"code\":-32600,\"message\":\"Invalid CSRF token!\"}}");
			output.close();
			return;
		}
		
		String currentUserId = (String) session.getAttribute("userId");
		if (currentUserId == null) {
			Utils.writeOutputStream(output, "{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{" + 
					"\"code\":-32600,\"message\":\"User not authorized.\"}}");
			output.close();
			return;
		}
		
		// otherwise...handle it.
	    jsonRpcServer.handle(request, response, currentUserId);
	}
	
	protected void initializeSession(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true); // Creates a new session.
		session.setMaxInactiveInterval(Utils.sessionTimeout); // Set the timeout
		
		// Overriding default session cookie to use HttpOnly, and Secure if we're on https
		response.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; HttpOnly; Path=/; max-age=" + Utils.sessionTimeout);
		
		// Generate a nonce and set it as a session attribute to be verified later.
		//String nonce = Utils.generateNonce(8);
		String nonce = Utils.getNonceFromSessionId(session.getId());
		session.setAttribute("nonce", nonce);
		
		// Send the nonce to the client as a cookie. Must be JS-readable.
		response.addHeader("Set-Cookie", "XSRF-TOKEN=" + nonce + "; Path=/; max-age=" + Utils.sessionTimeout);
	}
	
	@Override
	public void init(ServletConfig config) {
	    this.serviceService = new ServiceService();
	    this.jsonRpcServer = new ModifiedJsonRpcServer(this.serviceService, ServiceService.class);
	}
}