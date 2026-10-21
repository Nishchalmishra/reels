package arivihan.technologies.doubtbuzzter2.helper;


import com.google.gson.JsonObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Firebase Cloud Messaging (FCM) can be used to send messages to clients on iOS, Android and Web.
 *
 * This sample uses FCM to send two types of messages to clients that are subscribed to the `news`
 * topic. One type of message is a simple notification message (display message). The other is
 * a notification message (display notification) with platform specific customizations, for example,
 * a badge is added to messages that are sent to iOS devices.
 */
public class Messaging {

    private static final String PROJECT_ID = "doubtbuzzzter";
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    private static final String TITLE = "FCM Notification";
    private static final String BODY = "Notification from FCM";
    public static final String MESSAGE_KEY = "message";

    /**
     * Retrieve a valid access token that can be use to authorize requests to the FCM REST
     * API.
     *
     * @return Access token.
     * @throws IOException
     */
    // [START retrieve_access_token]
    // [END retrieve_access_token]

    /**
     * Create HttpURLConnection that can be used for both retrieving and publishing.
     *
     * @return Base HttpURLConnection.
     * @throws IOException
     */
    private static HttpURLConnection getConnection() throws IOException {
        // [START use_access_token]
        URL url = new URL(BASE_URL );
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "key=" + "AAAAEpq4udo:APA91bECVMzp3_3ZKFz9IhBOnYqIVEkj01SKd8k31plAD3Q-TQ3ixBErgwq7eK9ayMWNxOypvTdt7b1EE1E5J83Ur-s8lXJFsegdGFED_DTb4Yd9LI3eefTYjSdurX-FL-8Xuo7Sr4f-");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
        // [END use_access_token]
    }

    /**
     * Send request to FCM message using HTTP.
     *
     * @param fcmMessage Body of the HTTP request.
     * @throws IOException
     */
    public static void sendMessage(JsonObject fcmMessage)  {
        try {
            HttpURLConnection connection = getConnection();
            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(fcmMessage.toString());
            outputStream.flush();
            outputStream.close();

            int responseCode = 0;
                responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                String response = inputstreamToString(connection.getInputStream());
                System.out.println("Message sent to Firebase for delivery, response:");
                System.out.println(response);
            } else {
                System.out.println("Unable to send message to Firebase:");
                String response = inputstreamToString(connection.getErrorStream());
                System.out.println(response);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }
}
