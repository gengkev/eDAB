package com.desklampstudios.edab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.utils.SystemProperty;

public class GoogleOAuthClient {
	public static String getEndpointURL() {
    	boolean isProduction = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
    	if (isProduction) {
    		return "https://edab-ds.appspot.com";
    	} else {
    		return "http://localhost:8080";
    	}
	}
	public static String getEndpointURL(String host) {

    	String url = "https://accounts.google.com/o/oauth2/auth";
    	url += "?response_type=code";
    	url += "&client_id=808214402787.apps.googleusercontent.com";
    	url += "&redirect_uri=" + host + "/logincallback";    	
    	url += "&scope=https://www.googleapis.com/auth/userinfo.email";
    	url += "&hd=fcpsschools.net";
    	
    	return url;
	}
	
	protected static String getAccessToken(String authCode) throws IOException, ParseException {
    	// Open the connection to the access token thing
    	String params = "code=" + URLEncoder.encode(authCode, "UTF-8") + "&" +
    			"client_id=808214402787.apps.googleusercontent.com&" +
    			"client_secret=kB4ODOtZEftkoZKiqGOM47xy&" +
    			"redirect_uri=http://localhost:8888/createusercallback&" +
    			"grant_type=authorization_code";
    	
    	
        URL url = new URL("https://accounts.google.com/o/oauth2/token");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        
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
        String line, output = "";
        try {
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        
	        // dump it all into a string
	        while ((line = reader.readLine()) != null) {
	            output += line;
	            output += "\n";
	        }
        } catch (IOException e) {
        	throw e;
        	//resp.sendError(500, "IOException while reading input: " + e);
        } finally {
        	if (reader != null) {
        		reader.close();
        	}
        }
        
        
        // check if 200
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        	throw new IOException("Response code not 200- " + connection.getResponseCode() + "\n" + output);
        }
        
        // Parse the JSON.
        String access_token = null;
        try {
        	JSONParser parser = new JSONParser();
        	JSONObject obj = (JSONObject) parser.parse(output);
        	access_token = (String) obj.get("access_token");
        } catch (ParseException e) {
        	throw e;
        }
        
        return access_token;
	}
	
	protected static String getUserEmail(String access_token) throws IOException, ParseException {
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		String line, output = "";
		
		try {
			URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + access_token);
	        connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        // connection.setRequestProperty("Authorization", "Bearer " + access_token);
	        
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = reader.readLine()) != null) {
                output += line;
                output += "\n";
            }
            reader.close();
		} catch (IOException e) {
			throw e;
		}
		
		String email = null;
        try {
        	JSONParser parser = new JSONParser();
        	JSONObject obj = (JSONObject) parser.parse(output);
        	email = (String) obj.get("email");
        } catch (ParseException e) {
        	throw e;
        }
        
        return email;
	}
}
