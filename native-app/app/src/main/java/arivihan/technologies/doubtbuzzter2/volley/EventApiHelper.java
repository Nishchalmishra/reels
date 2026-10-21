package arivihan.technologies.doubtbuzzter2.volley;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes;
import arivihan.technologies.doubtbuzzter2.enums.NotificationEventType;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.DeviceInfoUtil;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.NotificationEventMetadata;
import arivihan.technologies.doubtbuzzter2.enums.event.ActionEventName;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods;

/*
 * File: EventApiHelper Copyright 18/04/26, 12:53 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 18/04/26, 12:53 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

/**
 * Helper to send action events to backend using existing Volley infrastructure.
 * This does not touch the local database and uses {@link MyJsonObjectRequest}.
 */
public class EventApiHelper {
    private static final String TAG = "EventApiHelper";

    public interface EventCallback {
        void onSuccess(@Nullable JSONObject response);

        void onFailure(@Nullable VolleyError error);
    }

    /**
     * Send an action event asynchronously using Volley.
     * This method is fire-and-forget when callback is null.
     *
     * @param event        event name
     * @param examSelected exam selected string
     * @param phoneNumber  phone number
     * @param data   data of following screen format : for example class : class:10:id
     * @param callback     optional callback for success/failure (can be null)
     */
    public static void sendActionEvent(String event,
                                       String examSelected,
                                       String phoneNumber,
                                       String data,
                                       String screenName,
                                       @Nullable EventCallback callback) {

        JSONObject body = new JSONObject();
        try {
            body.put("event", event == null ? JSONObject.NULL : event);
            arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel onboardingModel =
                    PreferenceMethods.getSelectedOnboardingModel();
            String examCode = (onboardingModel != null) ? onboardingModel.getExamCode() : null;
            body.put("examSelected", examCode == null ? JSONObject.NULL : examCode);
            body.put("phoneNumber", phoneNumber == null ? JSONObject.NULL : phoneNumber);
            body.put("value", data == null ? JSONObject.NULL : data); // was "data"
            body.put("screenName", screenName == null ? JSONObject.NULL : screenName);
            body.put("eventTime", System.currentTimeMillis());
            body.put("source", "CLIENT");
            body.put("secretKey", BuildConfig.EVENT_SECRET_KEY);
            body.put("appVersion", BuildConfig.VERSION_NAME);
            body.put("osVersion", Build.VERSION.RELEASE);
            body.put("platform", "ANDROID");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build event JSON", e);
            if (callback != null) {
                callback.onFailure(new VolleyError(e));
            }
            return;
        }

        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.POST,
                UrlRoutes.ACTION_EVENT,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Action event sent successfully: " + response);
                        if (callback != null) callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Failed to send action event", error);
                        if (callback != null) callback.onFailure(error);
                    }
                }
        );

        // Add to existing singleton request queue (async)
        MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(request);
    }

    /** Convenience fire-and-forget overload */
    public static void sendActionEvent(String event,
                                       String examSelected,
                                       String phoneNumber,
                                       String data,
                                       String screenName) {
        sendActionEvent(event, examSelected, phoneNumber, data, screenName, null);
    }

    /**
     * Specialized helper to log the initiation of a resume flow.
     * Captures the user's phone, the step being resumed to, and the activity context.
     *
     * @param phoneNumber  User's phone number
     * @param resumedStep  The screen/step name being restored (e.g. from state DTO)
     * @param contextName  The name of the activity or flow context
     */
    public static void logResumeFlowInitiated(String phoneNumber, String resumedStep, String contextName) {
        sendActionEvent(
                ActionEventName.RESUME_FLOW_INITIATED.getValue(),
                null, // internally handles fetching current examSelected from prefs
                phoneNumber,
                resumedStep,
                contextName
        );
    }


    public static void sendActionEventDismissNotification(Context context, NotificationEventMetadata metadata){
        sendActionEventNotification(context, metadata, NotificationEventType.NOTIFICATION_DISMISS.getValue(), "", "", "", null);
    }

    public static void sendActionEventDropNotification(Context context, NotificationEventMetadata metadata, String dropReason){
        sendActionEventNotification(context, metadata, NotificationEventType.NOTIFICATION_DROP.getValue(), "", "", dropReason, null);
    }

    public static void sendActionEventReceiveNotification(Context context, NotificationEventMetadata metadata){
        sendActionEventNotification(context, metadata, NotificationEventType.NOTIFICATION_RECEIVED.getValue(), "", "", "", null);
    }

    public static void sendActionEventNotificationClick(Context context, NotificationEventMetadata metadata, String eventType, String targetScreen, String landedScreen, String fallbackReason){
        sendActionEventNotification(context, metadata, eventType, targetScreen, landedScreen, fallbackReason, null);
    }

    private static void sendActionEventNotification(
            Context context,
            NotificationEventMetadata metadata,
            String eventType,
            String targetScreen,
            String landedScreen,
            String fallbackReason,
            @Nullable EventCallback callback
    ) {
        if (metadata == null) metadata = new NotificationEventMetadata();

        JSONObject body = new JSONObject();

        try {
            body.put("eventId", UUID.randomUUID().toString());

            //user data
            String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
            body.put("userId", userId == null ? "" : userId);

            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            body.put("batchCode", batchCode == null ? "" : batchCode);

            body.put("isSubscribedAtSend", !SubscriptionType.isBasicSubscription());
            body.put("deviceId", DeviceInfoUtil.getDeviceId(context));

            String deviceFcmToken = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
            body.put("deviceFcmToken", deviceFcmToken == null ? "" : getTokenFingerprint(deviceFcmToken));

            // Use metadata for all notification fields
            body.put("notificationId", metadata.notificationId == null ? "" : metadata.notificationId);
            body.put("deliveryMode", metadata.deliveryMode == null ? "" : metadata.deliveryMode);

            String fcmTopic = metadata.fcmTopic == null ? "" : metadata.fcmTopic;
            if (fcmTopic.startsWith("/topics/")) fcmTopic = fcmTopic.substring(8); // Remove "/topics/" prefix
            body.put("fcmTopic", fcmTopic);

            body.put("notificationType", metadata.notificationType == null ? "" : metadata.notificationType);

            //notification data
            body.put("variantTitle", metadata.title == null ? "" : metadata.title);
            body.put("variantBody", metadata.body == null ? "" : metadata.body);
            body.put("priority", metadata.priority == null ? "" : metadata.priority);

            body.put("ctaType", metadata.ctaType == null ? "" : metadata.ctaType);
            body.put("ctaValue", metadata.ctaValue == null ? "" : metadata.ctaValue);
            body.put("targetScreen", targetScreen == null ? "" : targetScreen);
            body.put("landedScreen", landedScreen == null ? "" : landedScreen);
            body.put("fallbackReason", fallbackReason == null ? "" : fallbackReason);

            body.put("eventType", eventType == null ? "" : eventType);
            body.put("sessionId", ""); //future scope
            body.put("attributes", metadataToJson(metadata));

            body.put("channel", "PUSH");
            body.put("eventTime", System.currentTimeMillis());
            body.put("appVersion", BuildConfig.VERSION_NAME);
            body.put("osVersion", Build.VERSION.RELEASE);
            body.put("platform", "ANDROID");
            body.put("eventSource", "ARIVIHAN_APP");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build notification event JSON", e);
            if (callback != null) callback.onFailure(new VolleyError(e));
            return;
        }

        String env = EnvironmentManager.getEnvironmentNameByUrl(MyApplication.getEndPoint()).toLowerCase();
        String baseUrl = FirebaseRemoteConfig.getInstance().getString("analytics_events_url" + "_" + env);


        MyJsonObjectRequest request = new MyJsonObjectRequest(
                Request.Method.POST,
                baseUrl + UrlRoutes.ACTION_EVENT_NOTIFICATION,
                body,
                response -> {
                    Log.d(TAG, "Notification event sent successfully: " + response);
                    if (callback != null) callback.onSuccess(response);
                },
                error -> {
                    Log.e(TAG, "Failed to send notification event" + error);
                    Log.e(TAG, "Failed to send notification event" + error.getMessage());
                    if (callback != null) callback.onFailure(error);
                },
                true
        );

        MyVolleySingleton.getInstance(MyApplication.getInstance()).addToRequestQueue(request);
    }

    public static String getTokenFingerprint(String token) {
        if (token == null || token.length() <= 20) return token;
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }

    private static JSONObject metadataToJson(NotificationEventMetadata metadata) {
        JSONObject json = new JSONObject();
        if (metadata == null) return json;

        try {
            java.lang.reflect.Field[] fields = NotificationEventMetadata.class.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(metadata);
                json.put(field.getName(), value == null ? "" : value);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting metadata to JSON", e);
        }
        return json;
    }


}

