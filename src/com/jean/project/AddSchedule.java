package com.jean.project;
/*servlet that allows centres to add schedule information via a web form
 * the servlet refreshes after a schedule has been input (or failed) into the system.
 * a passkey (pass) corresponding to the centre's name must be input correctly for 
 * the schedule to be added to the datastore correctly*/
import java.io.*;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class AddSchedule extends HttpServlet {
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    
    @SuppressWarnings("rawtypes")
	Enumeration paramNames = request.getParameterNames();
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  Entity en=new Entity("Schedule");
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
	//check if centre exists
	if (centreExists(en.getProperty("centreName"), en.getProperty("pass"))){
		//redirect to add schedule page
		response.getWriter().println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"3;URL=http://vaccineconnector.appspot.com/postForm.html\">Submitting schedule... <br/> ");
		datastore.put(en);
	}
	else{
		//redirect to add schedule page
		response.getWriter().println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"10;URL=http://vaccineconnector.appspot.com/postForm.html\">Sorry centre does not exist in our system... <br/> ");
	}
  }
  
  public boolean centreExists(Object name, Object pass){
	  boolean exists=false;
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  Query q=new Query("Centre");
	  q.addFilter("pass", Query.FilterOperator.EQUAL, pass);
	  q.addFilter("centreName", Query.FilterOperator.EQUAL, name);
	  PreparedQuery pq=datastore.prepare(q);
	  FetchOptions fetchOptions = FetchOptions.Builder.withLimit(100);
	  int cnt=pq.countEntities(fetchOptions);
	  if(cnt>0){
		  exists= true;
	  }
	  return exists;
  }
    
  public void doPost(HttpServletRequest request,
                     HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}