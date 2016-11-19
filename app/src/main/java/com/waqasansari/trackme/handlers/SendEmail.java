package com.waqasansari.trackme.handlers;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.waqasansari.trackme.utils.Config;

import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by WaqasAhmed on 10/19/2016.
 */
public class SendEmail extends AsyncTask<Void, Void, Void> {
    private Context context;
    private Session session;

    private String email, subject, message;

    private List<String> attachments;

    public SendEmail(Context context, String email, String subject, String message, List<String> attachments) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.attachments = attachments;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties props = new Properties();
        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");


        session = Session.getDefaultInstance(props,
                                        new javax.mail.Authenticator() {
                                            @Override
                                            protected PasswordAuthentication getPasswordAuthentication() {
                                                return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                                            }
                                        });

        try {
            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(new InternetAddress(Config.EMAIL));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mimeMessage.setSubject(subject);


            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Fill the message
            messageBodyPart.setText(message);

            // Create a multipart message
            Multipart multipart = new MimeMultipart("mixed");

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            for(int i=0; i < attachments.size(); i++) {
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachments.get(i));
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(attachments.get(i));
                multipart.addBodyPart(messageBodyPart);
            }


            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

            // Send the complete message parts
            mimeMessage.setContent(multipart);
            // Send message
            Transport.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Email Sent", Toast.LENGTH_SHORT).show();
    }

}
