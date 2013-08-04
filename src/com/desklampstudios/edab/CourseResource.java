package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/courses")
@Consumes("application/json")
@Produces("application/json")
public class CourseResource {
	private static final Logger log = Logger.getLogger(CourseResource.class.getName());
	
	@POST
	public Response createCourse(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws IOException {
		String currentUserId = AccountService.checkLogin(req, resp);
		
		/*
		// get provided course object
		InputStream inputStream = req.getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		Course providedCourse = mapper.readValue(inputStream, Course.class);
		
		// check for existing course
		long providedId = providedCourse.id;
		Course dbCourse = ofy().load().type(Course.class).id(providedId).now();
		if (dbCourse != null) {
			throw new eDABException.NotAuthorizedException("Cannot overwrite courses currently");
		}
		*/
		
		// create empty course
		Course providedCourse = new Course();
		
		log.log(Level.FINE, "Creating course " + providedCourse.toString());
		
		// otherwise add to db
		ofy().save().entity(providedCourse).now();
		
		// okayy
		return Response.ok().build();
	}
}
