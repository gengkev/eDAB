package com.desklampstudios.edab;

import java.io.BufferedReader;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;

public class ServiceServlet extends HttpServlet {
	static {
		ObjectifyService.register(User.class);
	}
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession();
		
		PrintWriter writer = resp.getWriter();
		
		BufferedReader br = null;
		StringBuilder input = new StringBuilder();
		String line;
		try {
			br = req.getReader();
			while ((line = br.readLine()) != null) {
				input.append(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			resp.setStatus(500);
			writer.print(createJsonRpcError(-32400, "System error", null));
			return;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	
		JSONObject obj;
		int id;
		String method;
		List<JSONValue> params;
		try {
			obj = (JSONObject) JSONValue.parse(input.toString());
		} catch (Exception e) {
			log.log(Level.WARNING, "JSON parse error", e);
			writer.print(createJsonRpcError(-32700, "Parse error", null));
			return;
		}
		try {
			assert obj.get("json-rpc") == "2.0";
			id = ((Long) obj.get("id")).intValue();
			method = (String) obj.get("method");
			params = (List<JSONValue>) obj.get("params");
		} catch (Exception e) {
			log.log(Level.WARNING, "Invalid JSON-RPC", e);
			resp.setStatus(400);
			writer.print(createJsonRpcError(-32600, "Invalid JSON-RPC", null));
			return;
		}
		
		
		Object result = null;
		
		log.info(method);
		
		try {
			if (method.equalsIgnoreCase("getUser")) {
				// get the current user
				result = getUser(session);
			} else {
				writer.print(createJsonRpcError(-32601, "Procedure not found", id));
				return;
			}
			writer.print("{\"jsonrpc\": \"2.0\", \"result\": " + JSONValue.toJSONString(result) + ", \"id\": " + id + "}");
		} catch(Exception e) {
			log.log(Level.WARNING, "um random error", e);
			writer.print(createJsonRpcError(-32400, "Error: " + e, id));
		}
	}
	protected static String createJsonRpcError(Integer code, String message, Integer id) {
		return "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": " + code + ", \"message\": \"" + message + "\"}, \"id\": " + id + "}";
	}
	protected static Object getUser(HttpSession session) throws Exception {
		String userId = (String) session.getAttribute("userId");
		if (userId == null) {
			throw new Exception("authentication_required");
		}
		User user = null;
		try {
			user = ofy().load().type(User.class).filter("fcps_id", userId).first().get(); // throws if not found
			if (user == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw e;
		}
		
		Map map = new LinkedHashMap();
		map.put("real_name", user.real_name);
		map.put("name", user.name);
		map.put("fcps_id", user.fcps_id);
		if (user.gender != null) {
			map.put("gender", user.gender.toString());
		}
		return map;
	}
}
