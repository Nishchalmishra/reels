package arivihan.technologies.doubtbuzzter2.helper.slack;/*
 * File: PotentialCustomerAlert Copyright 30/09/24, 1:04 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 30/09/24, 1:04 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import static arivihan.technologies.doubtbuzzter2.MyApplication.getBooleanValueFromRemoteConfig;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

public class PotentialCustomerAlert {

    Context context;

    public PotentialCustomerAlert(Context context) {
        this.context = context;
    }

    public void sendSubscriptionFailureAlert(String userId, String phoneNumber,
                                              String courseId, String classId,
                                              String subscriptionId, String amount,
                                              String paymentFailureReason) {
        boolean alertEnabled = false;
        String env = "";
        if (!BuildConfig.APP_UPDATE_POPUP) {
            alertEnabled = getBooleanValueFromRemoteConfig("slack_subscription_alert_dev_enabled");
            env = "DEV";
        } else {
            alertEnabled = getBooleanValueFromRemoteConfig("slack_subscription_alert_prod_enabled");
            env = "PROD";
        }

        if (!alertEnabled) {
            return;
        }

        // Create the JSON payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("text", "Subscription Failure Alert");

            JSONArray blocksArray = new JSONArray();

            // First block for the header
            JSONObject block1 = new JSONObject();
            block1.put("type", "section");
            JSONObject block1Text = new JSONObject();
            block1Text.put("type", "mrkdwn");
            block1Text.put("text", "*Subscription Failure Detected!* " + env);
            block1.put("text", block1Text);
            blocksArray.put(block1);

            // Second block for the details
            JSONObject block2 = new JSONObject();
            block2.put("type", "section");
            JSONArray fieldsArray = new JSONArray();

            // Add fields for the subscription details and failure reason
            fieldsArray.put(createField("*User ID:*", userId));
            fieldsArray.put(createField("*Phone Number:*", phoneNumber));
            fieldsArray.put(createField("*Course ID:*", courseId));
            fieldsArray.put(createField("*Class ID:*", classId));
            fieldsArray.put(createField("*Subscription ID:*", subscriptionId));
            fieldsArray.put(createField("*Amount:*", amount));
            fieldsArray.put(createField("*Payment Failure Reason:*", paymentFailureReason));

            block2.put("fields", fieldsArray);
            blocksArray.put(block2);

            // Add the blocks array to the payload
            payload.put("blocks", blocksArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String slackSubscriptionAlertUrl = CommonMethods.getObjectFromRemoteConfig("slack_subscription_alert_url");

        // Create a JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                slackSubscriptionAlertUrl,
                payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle successful response
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                }
        );

        // Add the request to the request queue
        MyVolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    // Helper method to create a field for Slack block
    private JSONObject createField(String title, String value) throws JSONException {
        JSONObject field = new JSONObject();
        field.put("type", "mrkdwn");
        field.put("text", title + "\n" + value);
        return field;
    }
}