package com.desklampstudios.edab;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.googlecode.objectify.ObjectifyService;

public class InitListener implements ServletContextListener {
	private static final Logger log = Logger.getLogger(InitListener.class.getName());
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		// ObjectifyService.register(Announcement.class);
		// ObjectifyService.register(Assignment.class);
		ObjectifyService.register(Course.class);
		ObjectifyService.register(Entry.class);
		// ObjectifyService.register(PersonalNote.class);
		ObjectifyService.register(School.class);
		ObjectifyService.register(User.class);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// appengine currently does not use this		
	}
}
