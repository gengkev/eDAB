package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;

public class ServiceService {
	static {
		ObjectifyService.register(User.class);
		ObjectifyService.register(Course.class);
		ObjectifyService.register(School.class);
		ObjectifyService.register(Entry.class);
	}
	
	private static final Logger log = Logger.getLogger(ServiceService.class.getName());
	
	static class UserNotFoundException extends Exception { }
	static class UserNeedsApprovalException extends Exception { }
	
	public static Object getCurrentUser(String currentUserId) throws Exception {
		return getUser(null, currentUserId);
	}
	public static Object getUser(String currentUserId, String userId) throws Exception {
		User user = ofy().load().group(User.LoadCourses.class).type(User.class).filter("fcps_id", userId).first().get();
		if (user == null) {
			throw new UserNotFoundException();
		}
		
		if (user.accountState == User.AccountState.NEEDS_APPROVAL) {
			throw new UserNeedsApprovalException();
		}
		
		return user;
	}
	public static Object setUserProperties(String currentUserId, HashMap<String, Object> userData) {
    	Ref<User> userRef = ofy().load().type(User.class).filter("fcps_id", currentUserId).first();
    	User user = userRef.get();
    	
    	if (userData.get("gender") != null) {
    		log.log(Level.FINE, "setting gender");
    		user.gender((String) userData.get("gender"));
    	}
    	if (userData.get("team") != null) {
    		log.log(Level.FINE, "setting gender");
    		user.team = (String) userData.get("team");
    	}
    	
    	ofy().save().entity(user).now();
    	return null;
	}
}
