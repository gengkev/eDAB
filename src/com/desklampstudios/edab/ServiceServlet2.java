package com.desklampstudios.edab;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;

public class ServiceServlet2 extends HttpServlet {
	static {
		ObjectifyService.register(User.class);
		ObjectifyService.register(Course.class);
		ObjectifyService.register(School.class);
		ObjectifyService.register(Entry.class);
	}
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter writer = resp.getWriter();
		String input = Utils.readInputStream(req.getInputStream());
		
		HttpSession session;
		String nonce;
		log.info(req.getRequestedSessionId() + "");
		if (req.isRequestedSessionIdValid()) {
			// we should be all set.
			session = req.getSession(false);
			nonce = (String) session.getAttribute("nonce");
		} else {
			session = req.getSession(true); // Creates a new session.
			session.setMaxInactiveInterval(Utils.sessionTimeout); // Set the timeout
			
			// Overriding default session cookie to use HttpOnly, and Secure if we're on https
			resp.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; HttpOnly; Path=/; max-age=" + Utils.sessionTimeout);
			
			// Generate a nonce and set it as a session attribute to be verified later.
			nonce = Utils.generateNonce(8);
			session.setAttribute("nonce", nonce);
			
			// Send the nonce to the client as a cookie. Must be JS-readable.
			resp.addHeader("Set-Cookie", "XSRF-TOKEN=" + nonce + "; Path=/; max-age=" + Utils.sessionTimeout);
			
			// Send response.
			writer.println(createError(ErrorCode.SESSION_INIT, "Initializing session, try again."));
			return;
		}
		
		String csrfHeader = req.getHeader("X-XSRF-Token");
		if (!csrfHeader.equals(nonce)) {
			writer.println(createError(ErrorCode.SESSION_TOKEN_INVALID, "CSRF token not found or invalid."));
			return;
		}
		
		String currentUserId = (String) session.getAttribute("userId");
		log.info(currentUserId);
		// now that we're done with session stuff lets actually do stuff
		
		JSONObject obj;
		String method;
		List<Object> params;
		try {
			obj = (JSONObject) JSONValue.parse(input.toString());
			method = (String) obj.get("method");
			params = (List<Object>) obj.get("params");
		} catch (Exception e) {
			log.log(Level.WARNING, "Parse error", e);
			resp.setStatus(400);
			writer.print(createError(ErrorCode.BAD_REQUEST, "Parse error; " + e.toString()));
			return;
		}
		
		Object result = null;
		
		
		if (method.equals("getUser") || method.equals("getCurrentUser")) {
			String userId = null;
			
			if (method.equals("getUser")) {
				if (params != null && params.size() > 0) {
					userId = (String) params.get(0);
				} else {
					writer.print(createError(ErrorCode.BAD_REQUEST, "Invalid params"));
					return;
				}
			} else {
				userId = currentUserId;
			}
			
			try {
				result = getUser(userId);
			} catch (Exception e) {
				writer.print(createError(ErrorCode.BAD_REQUEST, e.toString()));
				return;
			}
		} else {
			writer.print(createError(ErrorCode.BAD_REQUEST, "Method not found"));
			return;
		}
		
		LinkedHashMap<String,Object> resultObj = new LinkedHashMap<String,Object>();
		resultObj.put("result", result);
		writer.print(JSONValue.toJSONString(resultObj));
		
		writer.close();
	}
	protected static String createError(ErrorCode code, String detail) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		Map<String, String> error = new LinkedHashMap<String, String>();
		error.put("code", code.toString());
		error.put("detail", detail);
		result.put("error", error);
		
		return Utils.JsonPad + JSONValue.toJSONString(result);
	}
	
	static enum ErrorCode {
		SERVER_ERROR, BAD_REQUEST, SESSION_INIT, SESSION_TOKEN_INVALID;
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
	
	private static Object getUser(String userId) throws Exception {
		User user = ofy().load().type(User.class).filter("fcps_id", userId).first().get();
		if (user == null) {
			Exception e = new Exception("User not found");
			throw e;
		}
		
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", user.name);
		map.put("real_name", user.real_name);
		map.put("fcps_id", user.fcps_id);
		map.put("gender", user.getGender());
		return map;
	}
}
