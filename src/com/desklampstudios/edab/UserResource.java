package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/user")
@Consumes("application/json")
@Produces("application/json")
public class UserResource {
	
	@GET @Path("/{username}")
	public Response getUser(@PathParam("username") String username) throws JsonProcessingException {
		User user = getUserByUsername(username);
		if (user == null) {
			throw new eDABException.NotFoundException("The specified user could not be found");
		}
		
		// convert into json
		String json = new ObjectMapper().writeValueAsString(user);

		return Response.ok(Utils.JsonPad + json).build();
	}
	
	/**
	 * Finds a user given their username.
	 * Note that username is distinct from userId: userId is the Google id field, 
	 * while username can be that, or the fcps id prepended with ~.
	 * Example: 100630942498713537973 vs ~1390276
	 * This method tries to parse both forms.
	 * 
	 * @param username
	 * @return
	 */
	private User getUserByUsername(String username) {
		if (username.charAt(0) == '~') {
			// Try to find by fcps id
			return ofy().load().type(User.class).filter("fcps_id", username.substring(1)).first().now();
		} else {
			// Try to find by user id
			return ofy().load().type(User.class).id(username).now();
		}
	}
}
