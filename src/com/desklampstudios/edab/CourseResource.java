package com.desklampstudios.edab;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/courses")
@Consumes("application/json")
@Produces("application/json")
public class CourseResource {
	@POST
	public Response createCourse(@Context HttpServletRequest req, HttpServletResponse resp) {
		String currentUserId = AccountService.checkLogin(req, resp);
		return null;
	}
}
