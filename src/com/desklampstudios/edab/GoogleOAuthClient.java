package com.desklampstudios.edab;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.desklampstudios.edab.User.Gender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleOAuthClient {
	private static final Logger log = Logger.getLogger(GoogleOAuthClient.class.getName());
	
	// private static final String SERVER_HOST = "https://edab-ds.appspot.com";
	// private static final String DEBUG_HOST = "http://localhost:8888";
	private static final String LOGIN_PATH = "/logincallback";
	
	private static final String CLIENT_ID = "808214402787.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "dj908LVbvrMsHJb0KFZxL1tB";
	private static final String SCOPES = "https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email";
	
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
	
	protected static String getEndpointURL(String host, String state) {
    	String url = "https://accounts.google.com/o/oauth2/auth";
    	url += "?response_type=code";
    	url += "&client_id=" + CLIENT_ID;
    	url += "&redirect_uri=" + getRedirectURL(host);
    	url += "&scope=" + SCOPES;
    	url += "&hd=fcpsschools.net";
    	url += "&state=" + state;
    	
    	return url;
	}
	
	protected static String getAccessToken(String authCode, String host) throws Exception {
    	// Open the connection to the access token thing
    	String params = "code=" + URLEncoder.encode(authCode, "UTF-8") +
    			"&client_id=" + CLIENT_ID +
    			"&client_secret=" + CLIENT_SECRET +
    			"&redirect_uri=" + getRedirectURL(host) +
    			"&grant_type=authorization_code";
    	
    	String output = null;
    	try {
    		output = Utils.fetchURL(
    			"POST",
    			"https://accounts.google.com/o/oauth2/token",
    			params,
    			"application/x-www-form-urlencoded;charset=UTF-8");
    	} catch (IOException e) {
    		throw e;
    	}
    	
        // Parse the JSON.
        String access_token = null;
        try {
        	ObjectMapper m = new ObjectMapper();
        	JsonNode rootNode = m.readTree(output);
        	access_token = rootNode.path("access_token").textValue();
        } catch (Exception e) {
        	//log.log(Level.WARNING, "Error parsing JSON", e);
        	log.log(Level.INFO, "JSON that failed", output);
        	throw e;
        }
        
        if (access_token == null) {
        	//log.log(Level.WARNING, "No access token found in JSON!");
        	log.log(Level.INFO, "JSON that failed", output);
        	throw new Exception("No access token found in JSON!");
        }
        
        return access_token;
	}
	
	protected static User getUserData(String access_token) throws Exception {
		String output = null;
		
		try {
			output = Utils.fetchURL("GET", "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + access_token);
		} catch (IOException e) {
			throw e;
		}
		
        // Parse the JSON.
        User user = new User();
        try {
        	ObjectMapper m = new ObjectMapper();
        	JsonNode rootNode = m.readTree(output);
        	
        	String name = rootNode.path("name").textValue();
        	String email = rootNode.path("email").textValue();
        	boolean verifiedEmail = rootNode.get("verified_email").booleanValue();
        	String gender = rootNode.path("gender").textValue();
        	
        	if (name == null || email == null) {
        		throw new Exception("missing fields");
        	}
            // must be fcpsschools.net
            if (email.length() < 16 || !email.substring(email.length() - 16).equalsIgnoreCase("@fcpsschools.net") || 
            		verifiedEmail != true) {
            	throw new Exception("Invalid email address: " + email);
            }
            
            user.name = name;
            user.real_name = name;
            user.fcps_id = email.substring(0, email.length() - 16);
            
            if (gender != null) {
            	user.gender = Gender.valueOf(gender.toUpperCase());
            }
        } catch (Exception e) {
        	log.log(Level.INFO, "JSON that failed: " + output);
        	throw e;
        }
        
        return user;
	}
}
