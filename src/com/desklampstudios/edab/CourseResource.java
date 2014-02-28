package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
// import javax.ws.rs.core.Response;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.googlecode.objectify.Key;

@Path("courses")
@Consumes("application/json")
@Produces("application/json")
public class CourseResource {
	private static final Logger log = Logger.getLogger(CourseResource.class.getName());
	
	@GET
	public List<Course> listCourses() throws JsonProcessingException {
		List<Course> courses = ofy().load().type(Course.class).list();
		return courses;
	}
	
	@POST
	public Course createCourse(
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws IOException {
		
		@SuppressWarnings("unused")
		String currentUserId = AccountService.checkLogin(req, resp);
		
		// allocate id?
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Long id = ds.allocateIds("Course", 1).getStart().getId();
		
		// create empty course
		Course course = new Course(id);
		
		// yay logging		
		log.log(Level.INFO, "Creating course " + course.toString());
		
		// add to db
		ofy().save().entity(course).now();
		
		return course;
	}
	
	@GET @Path("/{courseId}")
	public Course getCourse(@PathParam("courseId") Long courseId) throws JsonProcessingException {
		Course course = ofy().load().type(Course.class).id(courseId).now();
		return course;
	}
	
	@PUT @Path("/{courseId}")
	public void setCourse(
			@Context HttpServletRequest req, 
			@Context HttpServletResponse resp,
			@PathParam("courseId") Long courseId) throws IOException {
		
		// Ahem hem ID please
		@SuppressWarnings("unused")
		String currentUserId = AccountService.checkLogin(req, resp);
		
		// load course id from db
		Course dbCourse = ofy().load().type(Course.class).id(courseId).now();
		
		// get provided course object
		InputStream inputStream = req.getInputStream();
		Course providedCourse = new ObjectMapper().readValue(inputStream, Course.class);
		
		// ...validate fields?
		try {
			assert providedCourse.getId() == dbCourse.getId(); // (dbCourse.id == courseId)
		} catch(AssertionError ex) {
			throw new eDABException.NotAuthorizedException("Illegal field change");
		}
		
		// save
		ofy().save().entity(providedCourse).now();
	}
	
	@GET @Path("/{courseId}/assignments")
	public List<Assignment> listAssignments(@PathParam("courseId") Long courseId) {		
		// likely to throw an exception (if courseId is invalid)
		Key<Course> course = Key.create(Course.class, courseId);
		List<Assignment> assignments = ofy().load().type(Assignment.class).ancestor(course).list();
		return assignments;
	}
	
	@POST @Path("/{courseId}/assignments")
	public Assignment createAssignment(
			@Context HttpServletRequest req, 
			@Context HttpServletResponse resp,
			@PathParam("courseId") Long courseId) {
		
		// Ahem hem ID please
		@SuppressWarnings("unused")
		String currentUserId = AccountService.checkLogin(req, resp);
		
		// get course to verify existence
		Course course = ofy().load().type(Course.class).id(courseId).now();
		
		// allocate id
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Long id = ds.allocateIds("Assignment", 1).getStart().getId();
		
		// create empty course
		Assignment asgn = new Assignment(id, Key.create(course));
		
		// yay logging		
		log.log(Level.INFO, "Creating assignment " + asgn.toString());
		
		// add to db
		ofy().save().entity(asgn).now();
		
		return asgn;
	}
	
	@GET @Path("/{courseId}/assignments/{asgnId}")
	public Assignment getAssignment(@PathParam("courseId") Long courseId, @PathParam("asgnId") Long asgnId) {
		Key<Course> courseKey = Key.create(Course.class, courseId);		
		Assignment asgn = ofy().load().type(Assignment.class).parent(courseKey).id(asgnId).now();
		return asgn;
	}
	
}
