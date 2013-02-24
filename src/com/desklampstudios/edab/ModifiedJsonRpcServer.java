package com.desklampstudios.edab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.NoCloseInputStream;
import com.googlecode.jsonrpc4j.NoCloseOutputStream;

// derp this is just copypasting a lot of code from JsonRpcServer
// so I can use the freaking user id
public class ModifiedJsonRpcServer extends JsonRpcServer {
    private static final Logger LOGGER = Logger.getLogger(ModifiedJsonRpcServer.class.getName());
    
	// whaat
	public ModifiedJsonRpcServer(Object handler, Class<?> remoteInterface) {
		super(handler, remoteInterface);
	}
	
	ObjectMapper mapper = new ObjectMapper();
	
	public void handle(HttpServletRequest request, HttpServletResponse response, String currentUserId)
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
		
		// modification to not crap out if there isn't a params object at all
		if (paramsNode == null || paramsNode.isNull()) {
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
