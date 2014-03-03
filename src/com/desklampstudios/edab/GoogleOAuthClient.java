package com.desklampstudios.edab;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import com.desklampstudios.edab.User.Gender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleOAuthClient {
	private static final Logger log = Logger.getLogger(GoogleOAuthClient.class.getName());

	private static final String LOGIN_PATH = "/logincallback";
	private static final String SCOPES = "profile email";

	private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
	private static final String TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/token";
	private static final String PROFILE_ENDPOINT = "https://www.googleapis.com/plus/v1/people/me";

	/*
	protected static String getRedirectURL() {
    	if (LoginCallbackServlet.isProduction) {
    		return getRedirectURL(SERVER_HOST);
    	} else {
    		return getRedirectURL(DEBUG_HOST);
    	}
	}
	 */
	protected static String getRedirectURL(String host) {
		return host + LOGIN_PATH;
	}
	protected static String getRequestHost(HttpServletRequest req) {
		return req.getScheme() + "://" +
				req.getServerName() + ":" + 
				req.getServerPort();
	}

	protected static String getEndpointURL(String host, String csrfToken) {
		UriBuilder uri = UriBuilder.fromUri(AUTH_ENDPOINT);
		uri.queryParam("response_type", "code");
		uri.queryParam("client_id", Config.CLIENT_ID);
		uri.queryParam("redirect_uri", getRedirectURL(host));
		uri.queryParam("scope", SCOPES);
		uri.queryParam("hd", "fcpsschools.net");
		uri.queryParam("state", csrfToken);

		return uri.build().toString();
	}

	protected static String getAccessToken(String authCode, String host) throws Exception {
		// Open the connection to the access token thing
		UriBuilder uri = UriBuilder.fromPath("");
		uri.queryParam("code", authCode);
		uri.queryParam("client_id", Config.CLIENT_ID);
		uri.queryParam("client_secret", Config.CLIENT_SECRET);
		uri.queryParam("redirect_uri", getRedirectURL(host));
		uri.queryParam("grant_type", "authorization_code");

		// substring(1) to get rid of the ? that's found in urls
		String params = uri.build().toString().substring(1);

		String output = null;
		try {
			output = Utils.fetchURL(
					"POST",
					TOKEN_ENDPOINT,
					params,
					"application/x-www-form-urlencoded;charset=UTF-8");
		} catch (IOException e) {
			throw e;
		}

		log.log(Level.FINER, "Google OAuth2 Token endpoint returned:\n" + output);

		// Parse the JSON.
		// TODO: Take advantage of the JWT.
		String access_token = null;
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode rootNode = m.readTree(output);
			access_token = rootNode.path("access_token").textValue();
		} catch (Exception e) {
			log.log(Level.INFO, "Error parsing JSON:\n" + output);
			throw e;
		}

		if (access_token == null) {
			log.log(Level.WARNING, "No access token found in JSON:\n" + output);
			throw new Exception("No access token found in JSON!");
		}

		return access_token;
	}

	protected static User getUserData(String access_token) throws Exception {
		// Call Google+ API people.get endpoint.
		String input = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(PROFILE_ENDPOINT);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "Bearer " + access_token);
		} catch (IOException e) {
			throw e;
		}

		// Send connection
		input = Utils.readInputStream(connection.getInputStream());

		log.log(Level.FINER, "Google people.get endpoint returned:\n" + input);

		// Parse the JSON.
		String id, name, email, gender;

		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode rootNode = m.readTree(input);

			id = rootNode.path("id").textValue();
			name = rootNode.path("displayName").textValue();
			email = rootNode.path("emails").get(0).path("value").textValue();
			gender = rootNode.path("gender").textValue();

			if (name == null || email == null) {
				throw new Exception("missing fields");
			}
			// must be fcpsschools.net
			if (email.length() < 16 || !email.substring(email.length() - 16).equalsIgnoreCase("@fcpsschools.net")) {
				throw new Exception("Invalid email address: " + email);
			}
		} catch (Exception e) {
			log.log(Level.INFO, "JSON that failed: " + input);
			throw e;
		}

		User user = new User(id);
		user.name = name;
		user.real_name = name;
		user.fcps_id = email.substring(0, email.length() - 16);

		if (gender != null) {
			user.gender = Gender.valueOf(gender.toUpperCase());
		}

		return user;
	}
}
