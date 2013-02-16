package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.LinkedHashMap;
import java.util.Map;

import com.googlecode.objectify.ObjectifyService;

public class ServiceService {
	static {
		ObjectifyService.register(User.class);
		ObjectifyService.register(Course.class);
		ObjectifyService.register(School.class);
		ObjectifyService.register(Entry.class);
	}
	public static Object getCurrentUser(String currentUserId) throws Exception {
		return getUser(null, currentUserId);
	}
	public static Object getUser(String currentUserId, String userId) throws Exception {
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
