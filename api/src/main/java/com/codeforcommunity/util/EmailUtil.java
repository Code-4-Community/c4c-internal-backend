package com.codeforcommunity.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*; //dependency
import javax.activation.*;

public class EmailUtil {

   public static void sendEmail(String email, String token) {
      // Recipient's email ID needs to be mentioned.
      String to = email;

      // Sender's email ID needs to be mentioned
      String from = "bhalla.v@husky.neu.edu"; //TODO: need to figure out

      // Assuming you are sending email from localhost
      String host = "localhost"; //TODO: need to figure out email host

      // Get system properties
      Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("mail.smtp.host", host);

      // Get the default Session object.
      Session session = Session.getDefaultInstance(properties);
      String hostname = "localhost";
      
      try {
        InetAddress ip = InetAddress.getLocalHost();
        hostname = ip.getHostName();
      }
      catch (UnknownHostException e) {
        e.printStackTrace();
      }

      try {
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

         // Set Subject: header field
         message.setSubject("Reset Password"); 
         
         String link = "http://" + hostname + ":8443/reset/password?qs=" + token; 
         // Send the actual HTML message, as big as you like
         message.setContent(link, "text/html"); //TODO: how to get link

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      } catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}
