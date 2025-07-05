package com.aoop.healthchain.util;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.concurrent.Task;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmail extends Task {

    private final String recepient;
    private final String subject;
    private final String content;

    public SendEmail(String recepient, String subject, String content) {
        this.recepient = recepient;
        this.subject = subject;
        this.content = content;
    }

    public static void sendMail(String recepient, String subject, String content) throws Exception {
        Dotenv dotenv = Dotenv.load();

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", dotenv.get("SMTP_HOST"));
        properties.put("mail.smtp.port", dotenv.get("SMTP_PORT"));

        String myAccountEmail = dotenv.get("SMTP_MAIL");
        String password = dotenv.get("SMTP_SECRET");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        Message message = prepareMessage(session, myAccountEmail, recepient, subject, content);

        if (message == null) {
            throw new RuntimeException("Failed to create email message. Check logs for details.");
        }

        Transport.send(message);
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recepient, String subject, String content) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            message.setSubject(subject);
            message.setContent(content, "text/html");
            return message;
        } catch (Exception e) {
            System.err.println("Error while preparing email message:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Object call() throws Exception {
        sendMail(recepient, subject, content);
        return null;
    }
}
