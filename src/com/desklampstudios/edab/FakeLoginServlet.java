package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class FakeLoginServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(Utils.class.getName());
	
	public static final String username = "TestUser";
	public static final String name = "John Doe";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {		
		User user = new User();
		user.id = username;
		user.fcps_id = username;
		user.name = name;
		user.real_name = name;
		user.gender = User.Gender.other;
		user.setBio("A test user");
		
		ofy().save().entity(user).now();
		
		HttpSession session = req.getSession(true);
		session.setAttribute("userId", username);
		
		session = AccountService.rotateSession(req, resp);
		
		log.log(Level.INFO, "Logged in fake user " + user.toString());
		
		resp.sendRedirect("/settings");
	}
}
