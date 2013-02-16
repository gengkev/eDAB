package com.desklampstudios.edab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
	private static final Logger log = Logger.getLogger(Utils.class.getName());
	
	// To be appended to JSON strings in responses as a deterrent to CSRF.
	public static final String JsonPad = ")]}',\n";
	
	public static final int sessionTimeout = 60 * 60 * 24 * 7; // 7 days in seconds
	
	// Short constructor for fetchURL that don't need to send data.
	public static String fetchURL(String method, String loadUrl) throws IOException {
		return fetchURL(method, loadUrl, null, null);
	}
	// A function to easily get data from URLs.
	public static String fetchURL(String method, String loadUrl, String data, String contentType) throws IOException {
		// Open the connection.
    	HttpURLConnection connection = null;
    	try {
	        URL url = new URL(loadUrl);
	        connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod(method);
	        if (contentType != null) {
	        	connection.setRequestProperty("Content-Type", contentType);
	        }
    	} catch (IOException e) {
    		throw e;
    	}
        
    	// If we are given data, write it.
    	if (data != null) {
    		connection.setDoOutput(true);
    		writeOutputStream(connection.getOutputStream(), data);
    	}
        
        // Get the input back.
        String input = readInputStream(connection.getInputStream());
        
        // Disconnect - probably isn't needed.
        connection.disconnect();
        
        return input;
	}
	
	// Helper function that writes a String to an OutputStream.
	public static void writeOutputStream(OutputStream outputStream, String output) throws IOException {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(outputStream);
            writer.write(output);
        } catch (IOException e) {
        	throw e;
        } finally {
        	if (writer != null) {
        		writer.close();
        	}
        }
	}
	
	// Helper function that reads a String from an InputStream.
	public static String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = null;
        StringBuilder inputBuilder = new StringBuilder();
        String line;
        try {
	        reader = new BufferedReader(new InputStreamReader(inputStream));
	        
	        // As long as there are still more lines, keep appending them.
	        while ((line = reader.readLine()) != null) {
	        	inputBuilder.append(line);
	        	inputBuilder.append("\n");
	        }
        } catch (IOException e) {
        	throw e;
        } finally {
        	if (reader != null) {
        		reader.close();
        	}
        }
        return inputBuilder.toString();
	}
	// Helper function that's close to encodeURIComponent in JS.
	// See http://stackoverflow.com/q/607176/689161
	public static String encodeURIComponent(String input) {
		String result = null;
		
		try {
			result = URLEncoder.encode(input, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			// this should never happen.
			log.log(Level.WARNING, "Something went wrong", e);
		}
		
		return result;
	}
	
	// helper function to generate a random nonce. Gets 8 bytes of random bits 
	// from SecureRandom, and converts it into hexadecimal to put into a StringBuilder.
	public static String generateNonce(int byteCount) {
		SecureRandom rand = new SecureRandom();
		byte[] bytes = new byte[byteCount];
		rand.nextBytes(bytes);
		
		StringBuilder nonceBuilder = new StringBuilder();
		for (byte b : bytes) {
			String hex = Integer.toHexString((int) (b + 128)); // shifts [-128, 127] to [0, 255]
			if (hex.length() < 2) {
				nonceBuilder.append("0");
			}
			assert hex.length() == 2;
			nonceBuilder.append(hex);
		}
		return nonceBuilder.toString();
	}
}
