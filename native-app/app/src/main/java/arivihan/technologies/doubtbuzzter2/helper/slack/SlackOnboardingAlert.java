package arivihan.technologies.doubtbuzzter2.helper.slack;

import static arivihan.technologies.doubtbuzzter2.MyApplication.getBooleanValueFromRemoteConfig;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class SlackOnboardingAlert {

    private static final String TAG = "SlackOnboardingAlert";
    Context context;
    String defaultMessage = "Not Filled";

    public SlackOnboardingAlert(Context context) {
        this.context = context;
    }

    public void sendOnboardingFailureAlert(String name, String number,
                                           String className,
                                           String course,
                                           String exam,
                                           String failureReason) {
//        if (StringUtils.isEmpty(name)) {
//            name = defaultMessage;
//        }
//        if (StringUtils.isEmpty(number)) {
//            number = defaultMessage;
//        }
//        if (StringUtils.isEmpty(className)) {
//            className = defaultMessage;
//        }
//        if (StringUtils.isEmpty(course)) {
//            course = defaultMessage;
//        }
//        if (StringUtils.isEmpty(exam)) {
//            exam = defaultMessage;
//        }
//
//        boolean alertEnabled = false;
//        String env = "";
//        if (!BuildConfig.APP_UPDATE_POPUP) {
//            alertEnabled = getBooleanValueFromRemoteConfig("slack_onboarding_alert_dev_enabled");
//            env = "DEV";
//        } else {
//            alertEnabled = getBooleanValueFromRemoteConfig("slack_onboarding_alert_prod_enabled");
//            env = "PROD";
//        }
//
//        Log.d(TAG, "Alert enabled: " + alertEnabled + ", Environment: " + env);
//
//        if (!alertEnabled) {
//            Log.d(TAG, "Alert is disabled. Exiting method.");
//            return;
//        }
//
//        String slackOnboardingAlertUrl = MyApplication.getFirebaseRemoteConfig().getString("slack_onboarding_alert_url");
//
//        if (slackOnboardingAlertUrl.isEmpty()) {
//            Log.e(TAG, "Slack webhook URL is empty or null. Cannot send alert.");
//            return;
//        }
//
//        Log.d(TAG, "Slack webhook URL: " + slackOnboardingAlertUrl);
//
//        // Create the JSON payload
//        JSONObject payload = createPayload(name, number, className, course, exam, failureReason, env);
//
//        // Create a JsonObjectRequest
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                Request.Method.POST,
//                slackOnboardingAlertUrl,
//                payload,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.d(TAG, "Alert sent successfully: " + response.toString());
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e(TAG, "Error sending alert: " + error.toString());
//                        if (error.networkResponse != null) {
//                            Log.e(TAG, "Error code: " + error.networkResponse.statusCode);
//                        }
//                    }
//                }
//        );
//
//        // Add the request to the request queue
//        MyVolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
//        Log.d(TAG, "Request added to queue");
    }

    private JSONObject createPayload(String name, String number, String className, String course, String exam, String failureReason, String env) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("text", "Onboarding Failure Alert");

            JSONArray blocksArray = new JSONArray();

            // First block for the header
            JSONObject block1 = new JSONObject();
            block1.put("type", "section");
            JSONObject block1Text = new JSONObject();
            block1Text.put("type", "mrkdwn");
            block1Text.put("text", "*Onboarding Failure Detected !* Environment = " + env);
            block1.put("text", block1Text);
            blocksArray.put(block1);

            // Second block for the details
            JSONObject block2 = new JSONObject();
            block2.put("type", "section");
            JSONArray fieldsArray = new JSONArray();

            // Add fields for the user details and failure reason
            fieldsArray.put(createField("*Name:*", name));
            fieldsArray.put(createField("*Number:*", number));
            fieldsArray.put(createField("*Class:*", className));
            fieldsArray.put(createField("*Course:*", course));
            fieldsArray.put(createField("*Exam:*", exam));
            fieldsArray.put(createField("*Failure Reason:*", failureReason));

            block2.put("fields", fieldsArray);
            blocksArray.put(block2);

            // Add the blocks array to the payload
            payload.put("blocks", blocksArray);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating payload: " + e.getMessage());
        }
        return payload;
    }

    // Helper method to create a field for Slack block
    private JSONObject createField(String title, String value) throws JSONException {
        JSONObject field = new JSONObject();
        field.put("type", "mrkdwn");
        field.put("text", title + "\n" + value);
        return field;
    }
}