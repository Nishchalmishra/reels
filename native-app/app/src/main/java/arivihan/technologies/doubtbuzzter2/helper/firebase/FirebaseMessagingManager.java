package arivihan.technologies.doubtbuzzter2.helper.firebase;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_USER_FCM_TOPICS_TO_SUBSCRIBE;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TOKEN_ATTACH_USER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TOKEN_DETACH_USER;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TOKEN_REFRESH;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.TOKEN_REGISTER;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import arivihan.technologies.doubtbuzzter2.BuildConfig;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType;
import arivihan.technologies.doubtbuzzter2.model.FirebaseTopicSubscription;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class FirebaseMessagingManager {


    private static final String TAG = "FirebaseMessagingManager";
    private static final String PREF_LAST_TOPIC_UPDATE_DATE = "last_topic_update_date";
    private static final String PREF_NOTIFICATION_DEVICE_ID = "notification_device_id";
    private static final String PREF_SUBSCRIBED_TOPICS = "subscribed_topics_list";



    public interface OnTokenFetchedListener {
        void onTokenFetched(String token);
        void onFailed(Exception e);
    }



    public interface OnTokenRefreshListener {
        void onTokenRefreshed(String newToken);
    }



    private static OnTokenRefreshListener tokenRefreshListener;

    // Device ID

    public static String getOrCreateDeviceId() {
        String deviceId = MyApplication.getStringData(PREF_NOTIFICATION_DEVICE_ID);
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            MyApplication.saveData(PREF_NOTIFICATION_DEVICE_ID, deviceId);
        }
        return deviceId;
    }


    // FCM token fetch & refresh listener

    public static void getFcmToken(Context context, OnTokenFetchedListener listener) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(fetchedToken -> {
                    String storedToken = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
                    if (!fetchedToken.equals(storedToken)) {
                        MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.FCM_TOKEN.getKey(), fetchedToken);
                        refreshToken(context, storedToken, fetchedToken);
                    } else {
                        Log.d(TAG, "getFcmToken: token unchanged, no update needed");
                    }
                    if (listener != null) {
                        listener.onTokenFetched(fetchedToken);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getFcmToken: failed to fetch token", e);
                    if (listener != null) {
                        listener.onFailed(e);
                    }
                });
    }

    public static void setOnTokenRefreshListener(OnTokenRefreshListener listener) {
        tokenRefreshListener = listener;
    }



    public static void notifyTokenRefresh(String newToken) {
        if (tokenRefreshListener != null) tokenRefreshListener.onTokenRefreshed(newToken);
    }



    // Token management APIs
    public static void registerToken(Context context) {
        String token = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
        if (StringUtils.isEmpty(token)) {
            Log.w(TAG, "registerToken: no FCM token in prefs, skipping");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("token", token);
            body.put("deviceId", getOrCreateDeviceId());
            body.put("platform", "ANDROID");

            MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(TOKEN_REGISTER, new HashMap<>()), body,
                    response -> Log.d(TAG, "registerToken: " + response),
                    error -> Log.e(TAG, "registerToken failed: " + error.getMessage(), error));
            request.setTag(TAG);
            MyVolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "registerToken: error building request body", e);
        }
    }



    public static void attachUser(Context context) {
        String token = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
        String userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());
        Log.d(TAG, "attachUser: fetched token = " + token);
        Log.d(TAG, "attachUser: fetched userId = " + userId);
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(userId)) {
            Log.w(TAG, "attachUser: token or userId missing in prefs, skipping");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("token", token);
            body.put("userId", userId);
            Log.d(TAG, "attachUser-body: "+body);

            MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(TOKEN_ATTACH_USER, new HashMap<>()), body,
                    response -> Log.d(TAG, "attachUser: " + response),
                    error -> Log.e(TAG, "attachUser failed: " + error.getMessage(), error));
            request.setTag(TAG);
            MyVolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "attachUser: error building request body", e);
        }
    }




    public static void detachUser(Context context) {
        String token = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
        if (StringUtils.isEmpty(token)) {
            Log.w(TAG, "detachUser: no FCM token in prefs, skipping");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);

        MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
                CommonMethods.getUrlParam(TOKEN_DETACH_USER, params), null,
                response -> Log.d(TAG, "detachUser: " + response),
                error -> Log.e(TAG, "detachUser failed: " + error.getMessage(), error));
        request.setTag(TAG);
        MyVolleySingleton.getInstance(context).addToRequestQueue(request);
    }



    public static void refreshToken(Context context, String oldToken, String newToken) {
        try {
            JSONObject body = new JSONObject();
            body.put("oldToken", StringUtils.defaultString(oldToken));
            body.put("newToken", newToken);
            body.put("deviceId", getOrCreateDeviceId());
            body.put("platform", "ANDROID");

            Log.i(TAG, "refreshToken: " + body);

            MyJsonObjectRequest request = new MyJsonObjectRequest(Request.Method.POST,
                    TOKEN_REFRESH, body,
                    response -> {
                        Log.d(TAG, "refreshToken: " + response);
                        MyApplication.saveSharedPreferencesDoNotDelete(PreferenceKeys.FCM_TOKEN.getKey(), newToken);
                    },
                    error -> Log.e(TAG, "refreshToken failed: " + error.getMessage(), error));
            request.setTag(TAG);
            MyVolleySingleton.getInstance(context).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e(TAG, "refreshToken: error building request body", e);
        }
    }




    private static Set<String> sanitizeTopics(List<String> topics) {
        Set<String> result = new HashSet<>();
        if (topics == null) return result;
        for (String topic : topics) {
            if (topic != null && !topic.trim().isEmpty()) {
                result.add(topic.trim());
            }
        }
        return result;
    }

    private static boolean isTodayDifferentFromLastUpdate() {
        String lastDate = MyApplication.getStringData(PREF_LAST_TOPIC_UPDATE_DATE);
        String today = getTodayDateString();

        if (lastDate == null) {
            return true;
        }
        return !today.equals(lastDate);
    }

    private static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }







    ////// : TOPIC SUBSCRIBE

    //get from server
    public static void getFirebaseTopicsToSubscribeUnsubscribe(Context context) {
        if (!isTodayDifferentFromLastUpdate()) {
            return;
        }

        MyJsonArrayRequest myJsonObjectRequest = new MyJsonArrayRequest(Request.Method.GET,
                GET_USER_FCM_TOPICS_TO_SUBSCRIBE, null,
                response -> {
                    try {
                        Gson gson = new Gson();
                        TypeToken<List<FirebaseTopicSubscription>> token = new TypeToken<>() {};
                        List<FirebaseTopicSubscription> subscriptions = gson.fromJson(response.toString(), token.getType());

                        List<String> subscribeTopics = new ArrayList<>();
                        List<String> unsubscribeTopics = new ArrayList<>();

                        for (FirebaseTopicSubscription item : subscriptions) {
                            if (item.isSubscribe()) subscribeTopics.add(item.getTopic());
                            else unsubscribeTopics.add(item.getTopic());
                        }

                        if (!isTodayDifferentFromLastUpdate()) {
                            return;
                        }

                        //sub and unsub
                        subscribeToTopics(subscribeTopics);
                        unsubscribeFromTopics(unsubscribeTopics);

                        // Track server-managed topics (won't be removed in local updates)
                        saveServerManagedTopics(subscribeTopics, unsubscribeTopics);

                        //saveTodayAsLastUpdateDate
                        MyApplication.saveData(PREF_LAST_TOPIC_UPDATE_DATE, getTodayDateString());
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }, new VolleyGlobalErrorListener(context) {
            @Override
            public void postError(VolleyError error) {
                Log.e(TAG, "Error fetching FCM topics: " + error.toString());
            }
        });
        myJsonObjectRequest.setTag(TAG);
        MyVolleySingleton.getInstance(context).addToRequestQueue(myJsonObjectRequest);
    }



    public static void updateUserTopics() {
        new Thread(() -> {
            try {
                // Quick check: if no changes needed, skip everything
                Set<String> requiredTopics = buildRequiredTopics();
                Set<String> currentTopics = new HashSet<>(getTrackedSubscribedTopics());

                if (requiredTopics.equals(currentTopics)) {
                    return; // No changes needed
                }

                Set<String> serverManagedTopics = getServerManagedTopics();

                // Calculate diffs
                Set<String> toSubscribe = new HashSet<>(requiredTopics);
                toSubscribe.removeAll(currentTopics);

                Set<String> toUnsubscribe = new HashSet<>(currentTopics);
                toUnsubscribe.removeAll(requiredTopics);
                toUnsubscribe.removeAll(serverManagedTopics);

                // Handle old subscription tier unsubscription
                String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
                String currentSubscriptionType = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());

                if (StringUtils.isNotEmpty(batchCode) && StringUtils.isNotEmpty(currentSubscriptionType)) {
                    for (SubscriptionType tier : SubscriptionType.values()) {
                        if (tier.name().equalsIgnoreCase(currentSubscriptionType)) continue;
                        String oldTierTopic = qualifyTopic(batchCode + "_" + tier.name());
                        if (currentTopics.contains(oldTierTopic) && !serverManagedTopics.contains(oldTierTopic)) {
                            toUnsubscribe.add(oldTierTopic);
                        }
                    }
                }

                if (!toUnsubscribe.isEmpty()) unsubscribeFromTopics(toUnsubscribe.stream().toList());
                if (!toSubscribe.isEmpty()) subscribeToTopics(toSubscribe.stream().toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    //server managed topics
    private static Set<String> getServerManagedTopics() {
        Set<String> topics = new HashSet<>();
        try {
            String topicsStr = MyApplication.getStringData("server_managed_topics");
            if (StringUtils.isNotEmpty(topicsStr)) {
                topics.addAll(Arrays.asList(topicsStr.split(",")));
            }
        } catch (Exception e) {
            Log.w(TAG, "Error loading server-managed topics", e);
        }
        return topics;
    }


    //save
    public static void saveServerManagedTopics(List<String> subscribeTopics, List<String> unsubscribeTopics) {
        try {
            Set<String> existing = getServerManagedTopics();
            existing.addAll(subscribeTopics);
            existing.removeAll(unsubscribeTopics);

            String topicsStr = existing.isEmpty() ? "" : String.join(",", existing);
            MyApplication.saveData("server_managed_topics", topicsStr);
        } catch (Exception e) {
            Log.e(TAG, "Error saving server-managed topics", e);
        }
    }



    private static Set<String> buildRequiredTopics() {
        Set<String> topics = new HashSet<>();

        String phone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
        if (StringUtils.isNotEmpty(phone)) topics.add(qualifyTopic(phone));

        String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
        if (StringUtils.isNotEmpty(batchCode)) topics.add(qualifyTopic(batchCode));

        String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
        if (StringUtils.isNotEmpty(lang)) topics.add(qualifyTopic(lang));

        String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
        if (StringUtils.isNotEmpty(examCode)) topics.add(qualifyTopic(examCode));

        String subscriptionType = MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());
        if (StringUtils.isNotEmpty(subscriptionType) && StringUtils.isNotEmpty(batchCode)) {
            topics.add(qualifyTopic(batchCode + "_" + subscriptionType));
        }

        return topics;
    }

    public static void unSubscribeToUserDefaultTopics() {
        try{
            String phone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey());
            if(StringUtils.isNotEmpty(phone)) unsubscribeFromTopic(qualifyTopic(phone));

            String batchCode = MyApplication.getStringData(PreferenceKeys.BATCH_CODE.getKey());
            if(StringUtils.isNotEmpty(batchCode)) unsubscribeFromTopic(qualifyTopic(batchCode));

            String lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey());
            if(StringUtils.isNotEmpty(lang)) unsubscribeFromTopic(qualifyTopic(lang));

            String examCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.getKey());
            if(StringUtils.isNotEmpty(examCode)) unsubscribeFromTopic(qualifyTopic(examCode));

            String batchSub = batchCode + "_" + MyApplication.getStringData(PreferenceKeys.USER_SUBSCRIPTION_TYPE.getKey());
            if(StringUtils.isNotEmpty(batchSub)) unsubscribeFromTopic(qualifyTopic(batchSub));

            //unsubscribe all the topic
            unsubscribeFromAllTrackedTopics();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private static String qualifyTopic(String topic) {
        if (MyApplication.getFirebaseRemoteConfig().getBoolean("enable_stage_environment_notification_topics") &&
            BuildConfig.BASE_URL_STAGE.equals(EnvironmentManager.getEndPoint()))
        {
            return topic + "_stage";
        }
        return topic;
    }



    public static void subscribeToTopics(@NonNull List<String> topics) {
        Set<String> uniqueTopics = sanitizeTopics(topics);
        for (String topic : uniqueTopics) subscribeToTopic(topic);
    }


    public static void unsubscribeFromTopics(@NonNull List<String> topics) {
        Set<String> uniqueTopics = sanitizeTopics(topics);
        for (String topic : uniqueTopics) unsubscribeFromTopic(topic);
    }


    public static void subscribeToTopic(@NonNull String topic) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        trackSubscribedTopic(topic);
                    }
                });
    }


    public static void unsubscribeFromTopic(@NonNull String topic) {
        FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(topic)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        untrackSubscribedTopic(topic);
                    }
                });
    }





    ///// Topic Tracking

    private static void trackSubscribedTopic(String topic) {
        try {
            String existingTopics = MyApplication.getDoNotDeleteSharedPrefKey(PREF_SUBSCRIBED_TOPICS);
            Set<String> topicSet = new HashSet<>();

            if (StringUtils.isNotEmpty(existingTopics)) {
                String[] topics = existingTopics.split(",");
                topicSet.addAll(java.util.Arrays.asList(topics));
            }

            topicSet.add(topic);
            String updatedTopics = String.join(",", topicSet);
            MyApplication.saveDataForDoNotDeleteSharedPreference(PREF_SUBSCRIBED_TOPICS, updatedTopics);

            Log.d(TAG, "Tracked topic: " + topic + " | Total: " + topicSet.size());
        } catch (Exception e) {
            Log.e(TAG, "Failed to track topic: " + topic, e);
        }
    }


    private static void untrackSubscribedTopic(String topic) {
        try {
            String existingTopics = MyApplication.getDoNotDeleteSharedPrefKey(PREF_SUBSCRIBED_TOPICS);
            if (StringUtils.isEmpty(existingTopics)) return;

            Set<String> topicSet = new HashSet<>(Arrays.asList(existingTopics.split(",")));
            topicSet.remove(topic);

            String updatedTopics = topicSet.isEmpty() ? "" : String.join(",", topicSet);
            MyApplication.saveDataForDoNotDeleteSharedPreference(PREF_SUBSCRIBED_TOPICS, updatedTopics);
            Log.d(TAG, "Untracked topic: " + topic + " | Remaining: " + topicSet.size());
        } catch (Exception e) {
            Log.e(TAG, "Failed to untrack topic: " + topic, e);
        }
    }

    public static List<String> getTrackedSubscribedTopics() {
        try {
            String topicsStr = MyApplication.getDoNotDeleteSharedPrefKey(PREF_SUBSCRIBED_TOPICS);
            if (StringUtils.isEmpty(topicsStr)) return Collections.emptyList();
            return Arrays.asList(topicsStr.split(","));
        } catch (Exception e) {
            Log.e(TAG, "Failed to get tracked topics", e);
            return Collections.emptyList();
        }
    }

    public static void unsubscribeFromAllTrackedTopics() {
        try {
            List<String> trackedTopics = getTrackedSubscribedTopics();
            if (trackedTopics.isEmpty()) return;

            for (String topic : trackedTopics) {
                if (StringUtils.isNotEmpty(topic.trim())) {
                    unsubscribeFromTopic(topic.trim());
                }
            }

            // Clear the tracking list
            MyApplication.saveDataForDoNotDeleteSharedPreference(PREF_SUBSCRIBED_TOPICS, "");
            Log.d(TAG, "✅ Cleared all tracked topics");
        } catch (Exception e) {
            Log.e(TAG, "Failed to unsubscribe from all tracked topics", e);
        }
    }


}
