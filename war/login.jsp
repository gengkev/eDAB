<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
	// response.sendRedirect is appending jsessionid, wtf?
	response.setStatus(302);
	response.setHeader("Location", 
			com.desklampstudios.edab.GoogleOAuthClient.getEndpointURL(
					request.getScheme() + "://" +
					request.getServerName() + ":" + 
					request.getServerPort()));
%>