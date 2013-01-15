<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.desklampstudios.edab.*" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
	// session stuff; gotta make it httponly, let's go
	String sessionid = session.getId();
	String cookie = "JSESSIONID=" + session.getId() + "; HttpOnly; Path=/; max-age=" + (60 * 60 * 24 * 7);
	if (request.getScheme() == "https") {
		cookie += "; Secure";
	}
	response.setHeader("Set-Cookie", cookie);


	// response.sendRedirect is appending jsessionid, wtf?
	// so here's a silly workaround.
	response.setStatus(302);
	response.setHeader("Location", GoogleOAuthClient.getEndpointURL(GoogleOAuthClient.getRequestHost(request)));
%>