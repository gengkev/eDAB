package com.desklampstudios.edab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class GoogleOAuthClient {
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
    	
    	HttpURLConnection connection = null;
    	try {
	        URL url = new URL("https://accounts.google.com/o/oauth2/token");
	        connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	} catch(IOException e) {
    		throw e;
    	}
        
        // Write the output to the connection, then close it
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(params);
        } catch (IOException e) {
        	throw e;
        } finally {
        	if (writer != null) {
        		writer.close();
        	}
        }
        
        
        // Get the input
        BufferedReader reader = null;
        StringBuilder output = new StringBuilder();
        String line;
        try {
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        
	        // dump it all into a string
	        while ((line = reader.readLine()) != null) {
	            output.append(line);
	        }
        } catch (IOException e) {
        	throw e;
        } finally {
        	if (reader != null) {
        		reader.close();
        	}
        }
        
        // check if 200
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        	//log.log(Level.WARNING, "Invalid response code: " + connection.getResponseCode(), output);
        	throw new IOException("Invalid response code: " + connection.getResponseCode());
        }
        
        // Parse the JSON.
        String access_token = null;
        try {
        	JSONObject obj = (JSONObject) JSONValue.parse(output.toString());
        	access_token = (String) obj.get("access_token");
        } catch (Exception e) {
        	//log.log(Level.WARNING, "Error parsing JSON", e);
        	log.log(Level.INFO, "JSON that failed", output);
        	throw e;
        }
        
        if (access_token == null || access_token.isEmpty()) {
        	//log.log(Level.WARNING, "No access token found in JSON!");
        	log.log(Level.INFO, "JSON that failed", output);
        	throw new Exception("No access token found in JSON!");
        }
        
        return access_token;
	}
	
	protected static String[] getUserData(String access_token) throws Exception {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		StringBuilder output = new StringBuilder();
		String line;
		
		try {
			URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + access_token);
	        connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        // connection.setRequestProperty("Authorization", "Bearer " + access_token);
	        
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append("\n");
            }
            reader.close();
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
        // Parse the JSON.
        String name = null, email = null;
        try {
        	JSONObject obj = (JSONObject) JSONValue.parse(output.toString());
        	name = (String) obj.get("name");
        	email = (String) obj.get("email");
        } catch (Exception e) {
        	//log.log(Level.WARNING, "Error parsing JSON", e);
        	log.log(Level.INFO, "JSON that failed", output);
        	throw e;
        }
        
        if (access_token == null || access_token.isEmpty()) {
        	//log.log(Level.WARNING, "No name/email found in JSON!");
        	log.log(Level.INFO, "JSON that failed", output);
        	throw new Exception("No name/email found in JSON!");
        }
        
        // must be fcpsschools.net
        if (!email.substring(email.length() - 16).equalsIgnoreCase("@fcpsschools.net")) {
        	//log.log(Level.INFO, "Invalid email address: " + email);
        	throw new Exception("Invalid email address: " + email);
        }
        
        String[] userData = {name, email.substring(0, email.length() - 16)};
        return userData;
	}
}
