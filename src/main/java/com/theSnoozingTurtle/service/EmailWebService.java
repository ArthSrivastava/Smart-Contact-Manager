package com.theSnoozingTurtle.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailWebService {
    public boolean sendEmail(String subject, String message, String to) {
        boolean sent = false;
        String from = "arthex254@gmail.com";
        //variable for gmail host
        String host = "smtp.gmail.com";

        //get the system properties
        Properties properties = System.getProperties();
        System.out.println("Properties:" + properties);

        //setting information to properties object
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        //step 1: to get the session object
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("arthex254@gmail.com", "iwclfohzjmkdamwd");
            }
        });
        session.setDebug(true);

        //step 2: compose the message
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            //from email
            mimeMessage.setFrom(from);

            //adding recipient to message
            mimeMessage.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(to)));

            //adding subject to message
            mimeMessage.setSubject(subject);

            //adding text to message
//            mimeMessage.setText(message);
            mimeMessage.setContent(message, "text/html");

            //step 3: send the message using Transport class
            Transport.send(mimeMessage);
            System.out.println("Successfully sent!");
            sent = true;
        } catch (MessagingException messagingException) {
            messagingException.printStackTrace();
            System.out.println("Some error occured");
        }
        return sent;
    }
}
