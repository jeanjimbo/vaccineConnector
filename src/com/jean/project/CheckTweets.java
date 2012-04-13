package com.jean.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/*
 * checks twitter for tweets from the previous day with the #vaccineconnector hashtag 
 * checks if they are from a vaccine centre in the datastore
 * adds the schedule to the datastore. this is a cron job that
 * occurs everyday at midnight - see cron.xml
 * */

@SuppressWarnings("serial")
public class CheckTweets extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text");
		//getting yesterday's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
		String date=dateFormat.format(cal.getTime());
        //enables us to search all tweets with vaccineconnector since yesterday's date
		String data="q=vaccineconnector&since="+date;

		try {		    
			//connect to twitter search
		    URL url = new URL("http://search.twitter.com/search.atom");
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    //read tweets
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    StringBuffer answer = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                answer.append(line);
            }
            String in=answer.toString();
            String[] tweets;
		    tweets=in.split("<title>");
		    if(tweets.length>2)
		    {
		    	String tweet="";
				String author="";
			    for(int x=2;x<tweets.length;x++)
			    {
			    	tweet=tweets[x].substring(0, tweets[x].indexOf("<"));
			    	author=(tweets[x].substring(tweets[x].indexOf("<name>")+6, tweets[x].indexOf("("))).trim();
			    	//check the author against the datastore
			    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				    Query q=new Query("Centre");
					q.addFilter("twitter", Query.FilterOperator.EQUAL, author);
					PreparedQuery pq=datastore.prepare(q);
					FetchOptions fo=FetchOptions.Builder.withLimit(50);
					int count=pq.countEntities(fo);
				    if(count>0){
				    	addSchedule(tweet);
			    	}
			    }
		    }
		    rd.close();
		    wr.close();
		} catch (Exception e) {
		}
	}
	
	public void addSchedule(Object tweet)
	{
		Entity en=new Entity("Schedule");
		tweet=tweet.toString().toLowerCase();
		String t=(String)tweet;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String disease, age,  location, riskgroup,  region, country, vacccode, vaccdescr, startdate, enddate;
		disease= age= location= riskgroup= region= country=  vacccode= vaccdescr= startdate= enddate="";
		String[] tokens= t.split(" ");
		 if(tokens.length>1)
		 {			
			 for(int x=1;x<tokens.length;x++)
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
				 if(tokens[x].lastIndexOf("vacccode:")!=-1)
				 {
					 vacccode=tokens[x].substring(9);
				 }
				 if(tokens[x].lastIndexOf("disease:")!=-1)
				 {
					 disease=tokens[x].substring(8);
				 }
				 if(tokens[x].lastIndexOf("region:")!=-1)
				 {
					 region=tokens[x].substring(7);
				 }
				 if(tokens[x].lastIndexOf("country:")!=-1)
				 {
					 country=tokens[x].substring(8);
				 }
				 if(tokens[x].lastIndexOf("enddate:")!=-1)
				 {
					 enddate=tokens[x].substring(8);
				 }
				 if(tokens[x].lastIndexOf("startdate:")!=-1)
				 {
					 startdate=tokens[x].substring(10);
				 }
				 if(tokens[x].lastIndexOf("vaccdescr:")!=-1)
				 {
					 vaccdescr=tokens[x].substring(10);
				 }
			 }
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
		 }
		  datastore.put(en);
	}
	
	public boolean centreExists(String twitterHandle){
		  boolean exists=false;
		  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		  Query q=new Query("Centre");
		  q.addFilter("twitter", Query.FilterOperator.EQUAL, twitterHandle);
		  PreparedQuery pq=datastore.prepare(q);
		  FetchOptions fetchOptions = FetchOptions.Builder.withLimit(100);
		  int cnt=pq.countEntities(fetchOptions);
		  if(cnt>0){
			  exists= true;
		  }
		  return exists;
	  }
}
