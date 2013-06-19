package com.desklampstudios.edab;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/account")
@Consumes("application/json")
@Produces("application/json")
public class AccountResource {

	@GET @Path("/loginState")
	public Response getLoginState(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		String currentUserId = null;

		try {
			currentUserId = AccountService.checkLogin(req, resp);
		} catch (eDABException.NotLoggedInException e) {
			// do nothing, it'll just be null
		}

		return Response.ok("{\"currentUserId\": \"" + currentUserId + "\"}").build();
	}

	@GET @Path("/settings")
	public String getSettings(@Context HttpServletRequest req) {
		return "";
	}

	@POST @Path("/settings")
	public String setSettings(@Context HttpServletRequest req) {
		return "";
	}

}
