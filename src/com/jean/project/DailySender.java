package com.jean.project;
/*A cron job performed daily at midday to find newly posted schedules that correspond
 * to requests from SMSs and sends an SMS to any requests that can be fulfilled*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@SuppressWarnings("serial")
public class DailySender extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{
		//get all sms requests
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String msg="";
		Query q=new Query("inboundSMS");
		//get list of sms 
		PreparedQuery pq=datastore.prepare(q);
		//get list of schedules
		for(Entity sms: pq.asIterable())
		{
			Query qq=new Query("Schedule");
			if(sms.getProperty("location")!=null){
				qq.addFilter("location", Query.FilterOperator.EQUAL, sms.getProperty("location").toString());
			}
			if(sms.getProperty("age")!=null  && !sms.getProperty("age").toString().equals("")){
				qq.addFilter("age", Query.FilterOperator.EQUAL, sms.getProperty("age").toString());
			}
			if(sms.getProperty("disease")!=null && !sms.getProperty("disease").toString().equals("")){
				qq.addFilter("disease", Query.FilterOperator.EQUAL, sms.getProperty("disease").toString());
			}
			if(sms.getProperty("riskgroup")!=null && !sms.getProperty("riskgroup").toString().equals(""))
			{
				qq.addFilter("riskgroup", Query.FilterOperator.EQUAL, sms.getProperty("riskgroup").toString());
			}
			PreparedQuery pq2=datastore.prepare(qq);
			for(Entity sched:pq2.asIterable()){
				if(sched.getProperty("startDate").toString().equalsIgnoreCase("All") && sched.getProperty("endDate").toString().equalsIgnoreCase("All"))
				  {
					  msg=sched.getProperty("disease").toString()+" vaccine is available at "+sched.getProperty("location").toString();
				  }
				  else if(sched.getProperty("startDate").toString().equalsIgnoreCase("All"))
				  {
					  msg=sched.getProperty("disease").toString()+" vaccine is available at "+sched.getProperty("location").toString()+" until "+sched.getProperty("endDate").toString();
				  }
				  else if(sched.getProperty("endDate").toString().equalsIgnoreCase("All"))
				  {
					  msg=sched.getProperty("disease").toString()+" vaccine is available at "+sched.getProperty("location").toString()+" from "+sched.getProperty("startDate").toString();
				  }
				  else
				  {
					  msg=sched.getProperty("disease").toString()+" vaccine is available at "+sched.getProperty("location").toString()+" from "+sched.getProperty("startDate").toString()+" to "+sched.getProperty("endDate").toString();
				  }
				  //SendSMS(sms.getProperty("number").toString(), msg);
				resp.getWriter().println(msg);
				  datastore.delete(sms.getKey());
			}
		}
	}
	
	//sends an sms
	public void SendSMS(String number, String message){
		HttpURLConnection connection = null;
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		StringBuilder sb = null;
		String line = null;
		
		URL serverAddress = null;
		
		try {
			serverAddress = new URL("https://rest.nexmo.com/sms/xml");
			//set up out communications stuff
			connection = null;
			
			//Set up the initial connection
			connection = (HttpURLConnection)serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(10000);
			
			connection.connect();
			
			String data="username=472edfb9&password=f21a7233&from=VaccCon&to="+number+"&text="+message; 
			//get the output stream writer and write the output to the server
			wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(data);
			wr.flush();
			
			//read the result from the server
			rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			sb = new StringBuilder();
			
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
			
			System.out.println(sb.toString());
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			//close the connection, set all objects to null
			connection.disconnect();
			rd = null;
			sb = null;
			wr = null;
			connection = null;
		}
	}
		
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doPost(req, resp);
	}
}


