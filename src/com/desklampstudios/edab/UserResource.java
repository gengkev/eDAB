package com.desklampstudios.edab;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
// import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.Key;

@Path("users")
@Consumes("application/json")
@Produces("application/json")
public class UserResource {
	
	@GET @Path("/{username}")
	public User getUser(@PathParam("username") String username) throws JsonProcessingException {
		Key<User> userKey = getUserByUsername(username);
		User user = ofy().load().key(userKey).now();
		
		if (user == null) {
			throw new eDABException.NotFoundException("The specified user could not be found");
		}
		
		return user;
		// return Response.ok(user).build();
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
	private Key<User> getUserByUsername(String username) {
		if (username.charAt(0) == '~') {
			// Try to find by fcps id
			return ofy().load().type(User.class).filter("fcps_id", username.substring(1)).keys().first().now();
		} else {
			// Try to find by user id
			return Key.create(User.class, username);
		}
	}
	
	@GET @Path("/me")
	public User getCurrentUser(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		// NotLoggedIn or InvalidSession sent to client
		String currentUserId = AccountService.checkLogin(req, resp);

		// now we must actually get the user info
		User user = ofy().load().type(User.class).id(currentUserId).now();
		
		assert user != null;

		if (user.accountState != null && user.accountState == User.AccountState.NEEDS_APPROVAL) {
			throw new eDABException.NeedsApprovalException("");
		}

		return user;
		// return Response.ok(user).build();
	}

	@PUT @Path("/me")
	public void setCurrentUser(
			@Context HttpServletRequest req,
			@Context HttpServletResponse resp) throws IOException {
		
		// NotLoggedIn or InvalidSession sent to client
		String currentUserId = AccountService.checkLogin(req, resp);
		
		// load user info from db
		User user = ofy().load().type(User.class).id(currentUserId).now();
		
		// get provided user object
		InputStream inputStream = req.getInputStream();
		User providedUser = new ObjectMapper().readValue(inputStream, User.class);
		
		// Make sure we're not changing fields we shouldn't be
		// Also ensures you can't change other people's
		try {
			assert user.getId() == providedUser.getId();
			assert user.name == providedUser.name;
			assert user.real_name == providedUser.real_name;
			assert user.fcps_id == providedUser.fcps_id;
		} catch (AssertionError ex) {
			throw new eDABException.NotAuthorizedException("Illegal field change");
		}
		
		// Save user
		ofy().save().entity(providedUser).now();
		
		// return Response.ok().build();
	}
}
