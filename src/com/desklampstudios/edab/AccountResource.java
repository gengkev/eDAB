package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("account")
@Consumes("application/json")
@Produces("application/json")
public class AccountResource {	
	
	@GET @Path("/currentUser")
	public Response getLoginState(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		String currentUserId = null;

		try {
			currentUserId = AccountService.checkLogin(req, resp);
		} catch (eDABException.NotLoggedInException e) {
			// send to client
			throw e;
		} catch (eDABException.InvalidSessionException e) {
			// send to client
			throw e;
		}
		
		// now we must actually get the user info
		
		User user = ofy().load().type(User.class).id(currentUserId).get();
		
		if (user.accountState == User.AccountState.NEEDS_APPROVAL) {
			throw new eDABException.NeedsApprovalException("");
		}
		
		// serialize into JSON
		
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(user);
		} catch (JsonProcessingException e) {
			throw new eDABException.InternalServerException(e.toString());
		}

		return Response.ok(Utils.JsonPad + json).build();
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
