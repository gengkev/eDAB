package com.desklampstudios.edab;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class LoginCallbackServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(LoginCallbackServlet.class.getName());
	
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	String error = req.getParameter("error");
    	String code = req.getParameter("code");
    	
    	if (error != null || code == null) {
    		resp.sendError(500, "Error!");
    		return;
    	}
    	
    	String access_token = null;
    	try {
    		access_token = GoogleOAuthClient.getAccessToken(code);
    	} catch (Exception e) {
    		resp.sendError(500, "Getting access token failed:\n\n" + e);
    	}
    	
    	String email = null;
    	try {
    		email = GoogleOAuthClient.getUserEmail(access_token);
    	} catch (Exception e) {
    		resp.sendError(500, "Getting email failed:\n\n" + e);
    	}
        
        // print the string to the output
    	resp.setContentType("text/plain");
    	PrintWriter writer = resp.getWriter();
    	writer.println("Auth code: " + code);
    	writer.println("Access token: " + access_token);
    	writer.println("Email address: " + email);
    	
    }
}