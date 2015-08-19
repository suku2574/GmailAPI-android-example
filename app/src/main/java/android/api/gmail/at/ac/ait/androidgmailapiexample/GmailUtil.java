package android.api.gmail.at.ac.ait.androidgmailapiexample;


import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailUtil {



    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to Email address of the receiver.
     * @param from Email address of the sender, the mailbox account.
     * @param subject Subject of the email.
     * @param bodyText Body text of the email.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */
    public static MimeMessage createEmail(String to, String from, String subject,
                                   String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
     * Create a Message from an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64 encoded email.
     * @throws IOException
     * @throws MessagingException
     */
    public static Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        email.writeTo(baos);
        String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }


    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param email Email to be sent.
     * @throws MessagingException
     * @throws java.io.IOException
     */
    public static void sendMessage(Gmail service, String userId, MimeMessage email)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(email);
        message = service.users().messages().send(userId, message).execute();

        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
    }


    /**
     * Get Message with given ID.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return Message Retrieved Message.
     * @throws java.io.IOException
     */
    public static Message getMessage(Gmail service, String userId, String messageId, String format) throws IOException {
        Message message = null;
        if(format != null && !format.isEmpty()) {
            message = service.users().messages().get(userId, messageId).setFormat(format).execute();
        } else {
            message = service.users().messages().get(userId, messageId).execute();
        }

        System.out.println("Message snippet: " + message.getSnippet());
        System.out.println("-----------------------------------------------****");
        System.out.println("Message raw: " + Util.base64UrlDecode(message.getRaw()));
        System.out.println("Message raw Base64 android: " + Base64.encodeBase64URLSafeString(message.decodeRaw()));
        System.out.println("-----------------------------------------------****");

        return message;
    }


    /**
     * List all Messages of the user's mailbox matching the query.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param query String used to filter the Messages listed.
     * @throws java.io.IOException
     */
    public static List<Message> listMessagesMatchingQuery(Gmail service, String userId, String query) throws IOException {
        System.out.println("listMessagesMatchingQuery");
        ListMessagesResponse response = service.users().messages().list(userId).setQ(query).execute();
        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setQ(query)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        for (Message message : messages) {
            System.out.println(message.toPrettyString());
        }

        return messages;
    }


    /**
     * List all Messages of the user's mailbox with labelIds applied.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param labelIds Only return Messages with these labelIds applied.
     * @throws java.io.IOException
     */
    public static List<Message> listMessagesWithLabels(Gmail service, String userId, List<String> labelIds) throws IOException {
        System.out.println("listMessagesWithLabels");
        ListMessagesResponse resp = service.users().messages().list(userId).execute();
        System.out.println("messages");
        System.out.println(resp.getMessages());


        ListMessagesResponse response = service.users().messages().list(userId)
                .setLabelIds(labelIds).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId).setLabelIds(labelIds)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        for (Message message : messages) {
            System.out.println(message.toPrettyString());
        }

        return messages;
    }


    /**
     * List all Messages of the user's mailbox with labelIds applied.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @throws java.io.IOException
     */
    public static List<Message> listAllMessages(Gmail service, String userId) throws IOException {
        System.out.println("listMessagesWithLabels");
        ListMessagesResponse response = service.users().messages().list(userId).execute();
        System.out.println("messages");
        System.out.println(response.getMessages());


        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(userId)
                        .setPageToken(pageToken).execute();
            } else {
                break;
            }
        }

        for (Message message : messages) {
            System.out.println(message.toPrettyString());
        }

        return messages;
    }


    public static List<String> getLabels(Gmail service, String userId){
        ListLabelsResponse listResponse = null;
        List<String> labelsStr = new ArrayList();

        try {
            listResponse = service.users().labels().list(userId).execute();List<Label> labels = listResponse.getLabels();

            if (labels.size() == 0) {
                System.out.println("No labels found.");
            } else {
                System.out.println("Labels:");
                for (Label label : labels) {
                    System.out.printf("- %s\n", label.getName());
                    labelsStr.add(label.getId());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return labelsStr;
    }
}