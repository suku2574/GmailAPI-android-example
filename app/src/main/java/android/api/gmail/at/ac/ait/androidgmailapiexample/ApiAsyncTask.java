package android.api.gmail.at.ac.ait.androidgmailapiexample;


import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Gmail API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private MainActivity mActivity;
    private GoogleAccountCredential credential;

    private static final String USER_ID = "me";
    private static final String TO = "test.caramelo@gmail.com";
    private static final String FROM = "test.caramelo@gmail.com";

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(MainActivity activity, GoogleAccountCredential credential) {
        this.mActivity = activity;
        this.credential = credential;
    }

    /**
     * Background task to call Gmail API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {

        /*
        try {
            String token = credential.getToken();
            Log.d("CredentialTask", "token:\n" + token);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GoogleAuthException e) {
            e.printStackTrace();
        }
        */

        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

            List<Message> messages = listAllMessages(USER_ID);
            if(messages != null && messages.size() > 0){
                GmailUtil.getMessage(mActivity.mService, USER_ID, messages.get(0).getId(), "raw");
            }

            GmailUtil.sendMessage(mActivity.mService, USER_ID, GmailUtil.createEmail(TO, FROM, "Subject", "Body"));

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    MainActivity.REQUEST_AUTHORIZATION);

        } catch (Exception e) {
            mActivity.updateStatus("The following error occurred:\n" +
                    e.getMessage());
        }
        return null;
    }

    /**
     * Fetch a list of Gmail labels attached to the specified account.
     * @return List of Strings labels.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {
        // Get the labels in the user's account.
        String user = "me";
        List<String> labels = new ArrayList<String>();
        ListLabelsResponse listResponse =
                mActivity.mService.users().labels().list(user).execute();
        for (Label label : listResponse.getLabels()) {
            labels.add(label.getName());
        }
        return labels;
    }

    /**
     * List all Messages of the user's mailbox with labelIds applied.
     *
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @throws IOException
     */
    public List<Message> listAllMessages(String userId) throws IOException {
        System.out.println("listMessagesWithLabels");

        ListMessagesResponse response = mActivity.mService.users().messages().list(userId).execute();

        List<Message> messages = new ArrayList<Message>();
        while (response.getMessages() != null) {
            messages.addAll(response.getMessages());
            if (response.getNextPageToken() != null) {
                String pageToken = response.getNextPageToken();
                response = mActivity.mService.users().messages().list(userId)
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
}
