package com.desklampstudios.edab;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(LoginServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session;
		if (req.isRequestedSessionIdValid()) {
			session = req.getSession(false); // we should not be creating a new session!
		} else {
			resp.sendError(500, "Session invalid");
			return;
		}

		// If we get passed close, save closeWindow to close the login window when we're done.
		if (req.getParameter("close") != null) {
			session.setAttribute("closeWindow", true);
		}

		// Get the CSRF token - is reusing it a good idea? :/ shrug
		String csrfToken = Utils.getCsrfTokenFromSessionId(session.getId());

		// For some reason, response.sendRedirect appends ;JSESSIONID=BLA
		// which is obviously not accepted by Google. So instead, let's
		// send our own 302 and Location header!
		resp.setStatus(302);
		resp.setHeader("Location", GoogleOAuthClient.getEndpointURL(
				GoogleOAuthClient.getRequestHost(req),
				csrfToken));
	}
}
