package com.smart.service;


import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String message, String to)
	{
		boolean f= false;
		
		String from="boyoffice000@gmail.com";
		
		String host="smtp.gmail.com";
		
		//Get System Properties
		Properties properties = System.getProperties();
		System.out.println("Properties : "+properties);
		
		//Host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port","465");
		properties.put("mail.smtp.ssl.enable","true");
		properties.put("mail.smtp.auth","true");
		
		//Step1 : to get Session object..
		Session session= Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("boyoffice000@gmail.com","Pass@5/6/1997");
			}
		});
		
		session.setDebug(true);
		
		//Step 2 : Compose the message
		MimeMessage m = new MimeMessage(session);
		try {
			//form email
			m.setFrom(from);
			
			//adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//Adding subject to message
			m.setSubject(subject);
			
			//Adding text message
			//m.setText(message);
			m.setContent(message,"text/html");
			//send
			//Sendmesage using transport class
			System.out.println("Message Sending");
			Transport.send(m);
			//System.out.println("Message Sent Successfully.........!");
			System.out.println("Send Successfully..........");
			f= true;
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return f;
	}
	
}
