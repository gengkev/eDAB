package com.desklampstudios.edab;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServiceServlet3 extends HttpServlet {
	private ServiceService serviceService;
	private ModifiedJsonRpcServer jsonRpcServer;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	    jsonRpcServer.handle(req, resp);
	}
	
	public void init(ServletConfig config) {
	    this.serviceService = new ServiceService();
	    this.jsonRpcServer = new ModifiedJsonRpcServer(this.serviceService, ServiceService.class);
	}
}