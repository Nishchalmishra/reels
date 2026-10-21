package arivihan.technologies.doubtbuzzter2.helper;

import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;

/*
 * File: ScreenAnalyticsHelper Copyright Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created by: Divyansh Sharma
 * Version: 1.0
 * Project Name: Arivihan
 */

/**
 * Central helper for all screen-level analytics API calls.
 * All methods are static and fire-and-forget.
 */
public class ScreenAnalyticsHelper {

    private static final String TAG = "ScreenAnalyticsHelper";
    private static final String EVENT_TYPE_APP_OPEN = "APP_OPEN";
    private static final String EVENT_TYPE_SCREEN_VISITED = "SCREEN_VISITED";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /**
     * Send once per calendar day when the user opens the application.
     */
    public static void sendAppOpenEvent() {
        JSONObject body = buildBasePayload(EVENT_TYPE_APP_OPEN, "HOME_SCREEN", "SCREEN");
        if (body == null) return;
        Log.d(TAG, "Sending body: " + body.toString());
        dispatch(body);
    }

    /**
     * Send whenever a user visits a trackable screen.
     *
     * @param screenName  name of the screen visited (e.g. "home")
     * @param contentType content type on that screen (e.g. "TOPPERS_NOTES"), may be null
     */
    public static void sendScreenVisitedEvent(String screenName, @Nullable String contentType) {
        JSONObject body = buildBasePayload(EVENT_TYPE_SCREEN_VISITED, screenName, contentType);
        if (body == null) return;
        dispatch(body);
    }

    // ─── private helpers ────────────────────────────────────────────────────────

    // Constant for this build – all events from the Android app
    private static final String DEVICE_TYPE = "ANDROID";

    @Nullable
    private static JSONObject buildBasePayload(String eventType, String screenName, String contentType) {
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "Skipping " + eventType + " — userId not available yet");
            return null;
        }
        long now = System.currentTimeMillis();

        // User / subscription attributes read from saved preferences
        String userName = MyApplication.getStringData(PreferenceKeys.NAME.getKey());
        String phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        String subscriptionPlan = String.valueOf(SubscriptionType.getSubscriptionType());
        boolean subscribed = SubscriptionType.hasActiveSubscription();

        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        String appVersion = BuildConfig.VERSION_NAME;

        try {
            JSONObject body = new JSONObject();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("eventType", eventType);
            body.put("userId", userId);
            body.put("eventDate", DATE_FORMAT.format(new Date(now)));
            body.put("screenName", screenName != null ? screenName : JSONObject.NULL);
            body.put("contentType", contentType != null ? contentType : JSONObject.NULL);
            body.put("timestamp", String.valueOf(now));
            body.put("deviceType", DEVICE_TYPE);
            body.put("deviceId", isNotEmpty(deviceModel) ? deviceModel : JSONObject.NULL);
            body.put("appVersion", isNotEmpty(appVersion) ? appVersion : JSONObject.NULL);
            body.put("eventTime", now);
            body.put("userName", isNotEmpty(userName) ? userName : JSONObject.NULL);
            body.put("phoneNumber", isNotEmpty(phoneNumber) ? phoneNumber : JSONObject.NULL);
            body.put("batchCode", isNotEmpty(batchCode) ? batchCode : JSONObject.NULL);
            body.put("subscribed", subscribed);
            body.put("subscriptionPlan", isNotEmpty(subscriptionPlan) ? subscriptionPlan : JSONObject.NULL);
            Log.d(TAG, "Payload → " + body);
            return body;
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build analytics payload", e);
            return null;
        }
    }

    private static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static void dispatch(JSONObject body) {
        String env = EnvironmentManager.getEnvironmentNameByUrl(
                MyApplication.getEndPoint()).toLowerCase();
        Log.d(TAG, "Environment: " + env);

        String remoteConfigKey = "analytics_events_url" + "_" + env;
        Log.d(TAG, "Remote Config Key: " + remoteConfigKey);

        String baseUrl = FirebaseRemoteConfig.getInstance().getString(remoteConfigKey);
        Log.d(TAG, "Analytics Base URL: " + baseUrl);

        String screenEventUrl = baseUrl + UrlRoutes.SCREEN_EVENT_PATH;
        Log.d(TAG, "Screen Event URL: " + screenEventUrl);

        Log.d(TAG, "Request Body: " + body);

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.POST,
                screenEventUrl,
                body,
                response -> {
                    Log.d(TAG, "Screen event sent successfully");
                    Log.d(TAG, "Response: " + response);
                },
                (VolleyError error) -> {
                    Log.e(TAG, "Failed to send screen event", error);

                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                        if (error.networkResponse.data != null) {
                            Log.e(TAG, "Error Response: " +
                                    new String(error.networkResponse.data));
                        }
                    } else {
                        Log.e(TAG, "No network response received");
                    }
                },
                true
        );

        Log.d(TAG, "Adding request to Volley queue");
        MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(request);
        Log.d(TAG, "Request added to Volley queue");
    }
}