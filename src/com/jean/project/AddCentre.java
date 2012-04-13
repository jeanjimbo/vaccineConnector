package com.jean.project;
/*servlet that allows system administrators to add centre information
 * the servlet refreshes after a centre has been input (or failed) into the system*/
import java.io.*;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class AddCentre extends HttpServlet {
	  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  
		response.setContentType("text/html");
		@SuppressWarnings("rawtypes")
		Enumeration paramNames = request.getParameterNames();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity en=new Entity("Centre");
		while(paramNames.hasMoreElements()){
			String paramName = (String)paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			//checks if paramValues is empty
			if (paramValues.length == 0)
			{
				en.setProperty(paramName, "null");
			}
			else
			{
				//concatenate parameter values
				String parval="";
				for(int x=0;x<paramValues.length;x++)
				{
					parval+=paramValues[x];
					
				}
				en.setProperty(paramName, parval.toLowerCase());
			}		
		}
		response.getWriter().println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"5;URL=http://vaccineconnector.appspot.com/postCentre.html\">Submitting centre... <br/>");
		datastore.put(en);
	}
		  
	  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  doGet(request, response);
	  }
}
