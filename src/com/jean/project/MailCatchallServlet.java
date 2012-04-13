package com.jean.project;
/*handles all emails sent to @vaccineconnector.appspotmail.com
 * Schedule information in emails is stored in datastore and other emails 
 * are forwarded to the admin account. */
import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Transport;
import javax.servlet.ServletException;


import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class MailCatchallServlet extends HttpServlet {
	public void doPost(HttpServletRequest req,
			HttpServletResponse resp)
			throws ServletException, IOException {

			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			try {
				MimeMessage message = new MimeMessage(session, req.getInputStream());
				DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				Entity ab=new Entity("Email");
				ab.setProperty("email", req.toString());
				datastore.put(ab);
				String msgBody = getText(message);
				Address[] addresses= message.getFrom();
				Address from=new InternetAddress();
				from=addresses[0];
				String subject=message.getSubject().toString();

				if (subject.equalsIgnoreCase("Schedule"))
				{
					//extract email address
					String add=addresses[0].toString();
					int i=add.indexOf("<");
					int k=add.indexOf(">");
					add=add.substring(i+1, k);

					//if the centre's email exists in system, add the schedule
					 if(centreExists(add))
					{		
						 String[] lines=msgBody.split("\n");
						 for(int j=0;j<lines.length;j++)
						 {
							 saveSchedule(lines[j]);	
						 }
							//sends a reply to the sender
						 reply(from, subject, "Schedule added successfully");
					 }
					 else{
						 reply(from, subject, "Your centre does not exist in our system. Please check your message and try again.");
					 }
				}
				else
				{
					//confirm email receipt and forward email to administrator email account
					reply(from, subject, "Thank you. Your message was received.");
					Address admin=new InternetAddress("vaccineconnector1@gmail.com");
					reply(admin, subject, msgBody);
				}
			} catch (Exception ex) {
			}
		}
	
	@SuppressWarnings("unused")
	private boolean textIsHtml = false;

	private String getText(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
		String s = (String)p.getContent();
		textIsHtml = p.isMimeType("text/html");
		return s;
		}
		if (p.isMimeType("multipart/alternative")) {
		Multipart mp = (Multipart)p.getContent();
		String text = null;
		for (int i = 0; i < mp.getCount(); i++) {
		    Part bp = mp.getBodyPart(i);
		    if (bp.isMimeType("text/plain")) {
		        if (text == null)
		            text = getText(bp);
		        continue;
		    } else if (bp.isMimeType("text/html")) {
		        String s = getText(bp);
		        if (s != null)
		            return s;
		    } else {
		        return getText(bp);
		    }
		}
		return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart)p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
			    String s = getText(mp.getBodyPart(i));
			    if (s != null)
			        return s;
			}
		}
		return null;
	}

	
	public void saveSchedule(String msgBody)
	{
		Entity en=new Entity("Schedule");
		msgBody.toLowerCase();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		String disease, age,  location, riskgroup,  region, country, vacccode, vaccdescr, startdate, enddate;
		disease= age= location= riskgroup= region= country=  vacccode= vaccdescr= startdate= enddate="";
		String[] tokens= msgBody.split(" ");
		 if(tokens.length>1)
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
	
	public boolean centreExists(String email){
		  boolean exists=false;
		  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		  Query q=new Query("Centre");
		  q.addFilter("email", Query.FilterOperator.EQUAL, email);
		  PreparedQuery pq=datastore.prepare(q);
		  FetchOptions fetchOptions = FetchOptions.Builder.withLimit(100);
		  int cnt=pq.countEntities(fetchOptions);
		  if(cnt>0){
			  exists= true;
		  }
		  return exists;
	  }

	public void reply(Address recipient, String subj, String msgBody ){
		Properties props = new Properties();
		Session email = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(email);
		try {
			message.setFrom(new InternetAddress("admin@vaccineconnector.appspotmail.com"));		
			message.addRecipient(Message.RecipientType.TO, recipient);
			message.setSubject(subj);
			message.setText(msgBody);
			Transport.send(message);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
