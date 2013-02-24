package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

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
		User user = ofy().load().group(User.LoadCourses.class).type(User.class).filter("fcps_id", userId).first().get();
		if (user == null) {
			Exception e = new Exception("User not found");
			throw e;
		}
		
		return user;
	}
}
