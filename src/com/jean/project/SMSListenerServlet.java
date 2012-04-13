/*Listens for incoming SMSs from either Vaccine Centres Sending in Schedule
 * data or Requests for vaccine information.
 * SMSs requesting vaccine information are matched with available vaccines
 * and a reply with the vaccine information is sent
 * If no information is found, the reply notifies the sender and then stores the SMS 
 * in the database, where later on if the requested information is available it will be
 * sent to the sender
 * SMSs with schedule information are verified by the sender's number before the vaccine 
 * information is added to the datastore. If the verification fails the sender is notified.
 * */

package com.jean.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class SMSListenerServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{
		/*Informs the SMS API that the request has been received*/
		resp.setStatus(HttpServletResponse.SC_OK);
		String number=req.getParameter("msisdn").toString();
		try{
			
			String request=req.getParameter("text").toString();
			request=URLDecoder.decode(request, "UTF-8");
			if(!request.equals(null))
			{
				String[] smsmsg=splitMsg(request, number);
				if(smsmsg[0].length()!=0)
				{
					if(smsmsg.length==1)
					{
						SendSMS(number, smsmsg[0]);
						if(smsmsg[0].equals("No vaccine found at the moment."))
						{
							storeSMS(req);
						}
					}
					else
					{
						String txt="";
						String txt2="";
						for(int x=0;x<smsmsg.length;x++)
						{
							txt+=smsmsg[x];
						}
						//take sms length into consideration
						int count=txt.length()/140;
						int count2=0;
						for(int x=0;x<txt.length();x+=140)
						{
							if(count2<count){
								txt2=txt.substring(x, x+140);
								SendSMS(number,txt2 );
							}
							else{
								txt2=txt.substring(x);
								SendSMS(number,txt2 );
							}
							count2++;
						}
					}
				}
			}
		}catch (Exception e){
			SendSMS(number, "There was a problem fulfilling your request, please try again later.");
		}
	}
	
	//splits the text of the message
	public String[] splitMsg(String msg, String number) throws IOException 
	{
		String[] match;
		String disease, age, location, riskgroup;
		disease=age=location=riskgroup=null;
		msg=msg.toLowerCase();
		String[] tokens= msg.split(",");
		 if(tokens.length>0)
		 {			
			//schedule sms
			 if(tokens[0].equalsIgnoreCase("schedule"))
			 {
				 String vacccode, region, country, enddate, startdate, vaccdescr, pass;
				 vacccode= region= country= enddate= startdate= vaccdescr=pass="";
				 for(int x=1;x<tokens.length;x++)
				 {
					 if(tokens[x].lastIndexOf("pass")!=-1)
					 {
						 pass=tokens[x].substring(4);
						 pass=pass.trim();
					 } 
					 if(tokens[x].lastIndexOf("age")!=-1)
					 {
						 age=tokens[x].substring(3);
						 age=age.trim();
					 }
					 if(tokens[x].lastIndexOf("riskgroup")!=-1)
					 {
						 riskgroup=tokens[x].substring(9);
						 riskgroup=riskgroup.trim();
					 }
					 if(tokens[x].lastIndexOf("location")!=-1)
					 {
						 location=tokens[x].substring(8);
						 location=location.trim();
					 }
					 if(tokens[x].lastIndexOf("vacccode")!=-1)
					 {
						 vacccode=tokens[x].substring(8);
						 vacccode=vacccode.trim();
					 }
					 if(tokens[x].lastIndexOf("disease")!=-1)
					 {
						 disease=tokens[x].substring(7);
						 disease=disease.trim();
					 }
					 if(tokens[x].lastIndexOf("region")!=-1)
					 {
						 region=tokens[x].substring(6);
						 region=region.trim();
					 }
					 if(tokens[x].lastIndexOf("country")!=-1)
					 {
						 country=tokens[x].substring(7);
						 country=country.trim();
					 }
					 if(tokens[x].lastIndexOf("enddate")!=-1)
					 {
						 enddate=tokens[x].substring(7);
						 enddate=enddate.trim();
					 }
					 if(tokens[x].lastIndexOf("startdate")!=-1)
					 {
						 startdate=tokens[x].substring(9);
						 startdate=startdate.trim();
					 }
					 if(tokens[x].lastIndexOf("vaccdescr")!=-1)
					 {
						 vaccdescr=tokens[x].substring(9);
						 vaccdescr=vaccdescr.trim();
					 }
				 }
				 if (centreExists(number, pass))
				 {
					DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
					Entity en=new Entity("Schedule");
					 en.setProperty("country", country);
					  en.setProperty("region", region);
					  en.setProperty("riskGroup", riskgroup);
					  en.setProperty("disease", disease);
					  en.setProperty("location", location);
					  en.setProperty("age", age);
					  en.setProperty("endDate", enddate);
					  en.setProperty("vaccCode", vacccode);
					  en.setProperty("vaccDescr", vaccdescr);
					  en.setProperty("startDate", startdate);
					  datastore.put(en);
					  match=new String[1];
					  match[0]="Schedule added successfully";
				 }
				 else
				 {
					 match=new String[1];
					 match[0]="Your pass does not match your number. Try again.";
				 }	
			 }
			 //request sms
			 else
			 {
				 for(int x=0;x<tokens.length;x++)
				 {
					 if(tokens[x].lastIndexOf("age:")!=-1)
					 {
						 age=tokens[x].substring(4);					 
					 }
					 if(tokens[x].lastIndexOf("riskgroup:")!=-1)
					 {
						 riskgroup=tokens[x].substring(10);
					 }
					 if(tokens[x].lastIndexOf("location:")!=-1)
					 { 
						 location=tokens[x].substring(9);
					 }
					 if(tokens[x].lastIndexOf("disease:")!=-1)
					 {						 
						 disease=tokens[x].substring(8);
					 }
				 }
				 /*Queries*/
				 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				 Query q=new Query("Schedule");	 
				 if(age!=null)
				 {
					 q.addFilter("age", Query.FilterOperator.EQUAL, age);
				 }
				 if(location!=null)
				 {
					 q.addFilter("location", Query.FilterOperator.EQUAL, location);
				 }
				 if(disease!=null)
				 {
					 q.addFilter("disease", Query.FilterOperator.EQUAL, disease);
				 }
				 if(riskgroup!=null)
				 {
					 q.addFilter("riskGroup", Query.FilterOperator.EQUAL, riskgroup);
				 }
				 
				 PreparedQuery pq=datastore.prepare(q);
				  int k=0;
				  FetchOptions fetchOptions = FetchOptions.Builder.withLimit(100);
				  int cnt=pq.countEntities(fetchOptions);
				  if(cnt>0)
				  {
					  match=new String[cnt];
					  for (Entity result : pq.asIterable()) {
						  if(result.getProperty("startDate").toString().equalsIgnoreCase("All") && result.getProperty("endDate").toString().equalsIgnoreCase("All"))
						  {
							  match[k]=result.getProperty("disease").toString()+" vaccine is available at "+result.getProperty("location").toString();
						  }
						  else if(result.getProperty("startDate").toString().equalsIgnoreCase("All"))
						  {
							  match[k]=result.getProperty("disease").toString()+" vaccine is available at "+result.getProperty("location").toString()+" to "+result.getProperty("endDate").toString();
						  }
						  else
						  {
							  match[k]=result.getProperty("disease").toString()+" vaccine is available at "+result.getProperty("location").toString()+" from "+result.getProperty("startDate").toString()+" to "+result.getProperty("endDate").toString();
						  }
						  k++;
					  }	
				  }
				  else
				  {
					  match=new String[1];
					  match[0]="No vaccine found at the moment.";
				  }
			 }
		 }
		 else
		 {
			match=new String[1];
			match[0]="";
		 }
		 String[] str=match;
		 return str;
	}
	
	public boolean centreExists(Object number, Object pass){
		  boolean exists=false;
		  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		  Query q=new Query("Centre");
		  q.addFilter("pass", Query.FilterOperator.EQUAL, pass);
		  q.addFilter("number", Query.FilterOperator.EQUAL, number);
		  PreparedQuery pq=datastore.prepare(q);
		  FetchOptions fetchOptions = FetchOptions.Builder.withLimit(100);
		  int cnt=pq.countEntities(fetchOptions);
		  if(cnt>0){
			  exists= true;
		  }
		  return exists;
	  }
	
	public void storeSMS(HttpServletRequest req)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String msg="";
		
		String disease, age, location, riskgroup, vaccine;
		disease=age=location=riskgroup=vaccine=null;
		Entity en=new Entity("inboundSMS");
		  en.setProperty("number", req.getParameter("msisdn"));
		  msg=req.getParameter("text").toLowerCase();

		 String[] tokens= msg.split(",");
		 if(tokens.length>1)
		 {			
			 for(int x=0;x<tokens.length;x++)
			 {
				 //if we find age in the token, we can extract the age
				 if(tokens[x].lastIndexOf("age")!=-1)
				 {
					 age=tokens[x].substring(3);
					 age=age.trim();
				 }
				 if(tokens[x].lastIndexOf("riskgroup")!=-1)
				 {
					 riskgroup=tokens[x].substring(9);
					 riskgroup=riskgroup.trim();
				 }
				 if(tokens[x].lastIndexOf("location")!=-1)
				 {
					 location=tokens[x].substring(8);
					 location=location.trim();
				 }
				 if(tokens[x].lastIndexOf("vaccine")!=-1)
				 {
					 vaccine=tokens[x].substring(7);
					 vaccine=vaccine.trim();
				 }
				 if(tokens[x].lastIndexOf("disease")!=-1)
				 {
					 disease=tokens[x].substring(7);
					 disease=disease.trim();
				 }
			 }
			  en.setProperty("riskGroup", riskgroup);
			  en.setProperty("disease", disease);
			  en.setProperty("location", location);
			  en.setProperty("age", age);
			  en.setProperty("vaccine", vaccine);
		 }
		  datastore.put(en);
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
