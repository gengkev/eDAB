package com.desklampstudios.edab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.NoCloseInputStream;
import com.googlecode.jsonrpc4j.NoCloseOutputStream;

public class ModifiedJsonRpcServer extends JsonRpcServer {
    private static final Logger LOGGER = Logger.getLogger(ModifiedJsonRpcServer.class.getName());
    
	// whaat
	public ModifiedJsonRpcServer(Object handler, Class<?> remoteInterface) {
		super(handler, remoteInterface);
	}
	
	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Handing HttpServletRequest "+request.getMethod());
		}

		// set response type
		response.setContentType(JSONRPC_RESPONSE_CONTENT_TYPE);

		// setup streams
		InputStream input       = null;
		OutputStream output     = response.getOutputStream();

		// POST
		if (request.getMethod().equals("POST")) {
			input = request.getInputStream();

			// GET
		} else if (request.getMethod().equals("GET")) {
			input = createInputStream(
					request.getParameter("method"),
					request.getParameter("id"),
					request.getParameter("params"));

			// invalid request
		} else {
			throw new IOException(
					"Invalid request method, only POST and GET is supported");
		}


		/** BEGIN MODIFICATION **/
		HttpSession session;
		String nonce;
		if (request.isRequestedSessionIdValid()) {
			// we should be all set.
			session = request.getSession(false);
			nonce = (String) session.getAttribute("nonce");
		} else {
			session = request.getSession(true); // Creates a new session.
			session.setMaxInactiveInterval(Utils.sessionTimeout); // Set the timeout
			
			// Overriding default session cookie to use HttpOnly, and Secure if we're on https
			response.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; HttpOnly; Path=/; max-age=" + Utils.sessionTimeout);
			
			// Generate a nonce and set it as a session attribute to be verified later.
			nonce = Utils.generateNonce(8);
			session.setAttribute("nonce", nonce);
			
			// Send the nonce to the client as a cookie. Must be JS-readable.
			response.addHeader("Set-Cookie", "XSRF-TOKEN=" + nonce + "; Path=/; max-age=" + Utils.sessionTimeout);
			
			// Send response.
			this.writeAndFlushValue(
					output, this.createErrorResponse(
							"2.0", "null", -32600, "Creating session, please retry.", null));
			return;
		}
		
		String csrfHeader = request.getHeader("X-XSRF-Token");
		if (!csrfHeader.equals(nonce)) {
			this.writeAndFlushValue(
					output, this.createErrorResponse(
							"2.0", "null", -32600, "Invalid CSRF Token", null));
			return;
		}
		
		String currentUserId = (String) session.getAttribute("userId");
		if (currentUserId == null) {
			this.writeAndFlushValue(
					output, this.createErrorResponse(
							"2.0", "null", -32600, "User not authorized.", null));
			return;
		}

		// service the request
		handle(input, output, currentUserId);
		/** END MODIFICATION **/
	}
	
	public void handle(InputStream ips, OutputStream ops, String currentUserId) throws IOException {
		/** BEGIN MODIFICATION **/
		JsonNode node = mapper.readTree(new NoCloseInputStream(ips));
		handleNode(node, ops, currentUserId);
		/** END MODIFICATION **/
	}
	
	public void handleNode(JsonNode node, OutputStream ops, String currentUserId)
			throws IOException {
		// handle objects
		if (node.isObject()) {
			/** BEGIN MODIFICATION **/
			handleObject(ObjectNode.class.cast(node), ops, currentUserId);
			/** END MODIFICATION **/

			// handle arrays
		} else if (node.isArray()) {
			/** BEGIN MODIFICATION **/
			handleArray(ArrayNode.class.cast(node), ops, currentUserId);
			/** END MODIFICATION **/

			// bail on bad data
		} else {
			this.writeAndFlushValue(
					ops, this.createErrorResponse(
							"2.0", "null", -32600, "Invalid Request", null));
		}
	}
	
	public void handleArray(ArrayNode node, OutputStream ops, String currentUserId)
			throws IOException {
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Handing "+node.size()+" requests");
		}

		// loop through each array element
		ops.write('[');
		for (int i=0; i<node.size(); i++) {
			/** BEGIN MODIFICATION **/
			handleNode(node.get(i), ops, currentUserId);
			/** END MODIFICATION **/
			if (i != node.size() - 1) ops.write(','); 
		}
		ops.write(']');
	}
	
	public void handleObject(ObjectNode node, OutputStream ops, String currentUserId)
			throws IOException {
		JsonNode paramsNode = node.get("params");
		ArrayNode paramsArrNode;
		
		if (paramsNode.isNull()) {
			paramsArrNode = mapper.createArrayNode();
		} else if (paramsNode.isArray()) {
			paramsArrNode = (ArrayNode) paramsNode;
		} else {
			writeAndFlushValue(ops, createErrorResponse(
					"2.0", "null", -32600, "Invalid Request", null));
			return;
		}
		
		paramsArrNode.insert(0, currentUserId);
		
		node.put("params", paramsArrNode);
		
		super.handleObject(node, ops);
	}
	
    private void writeAndFlushValue(OutputStream ops, Object value)
            throws IOException {
            mapper.writeValue(new NoCloseOutputStream(ops), value);
            ops.flush();
    }
}
