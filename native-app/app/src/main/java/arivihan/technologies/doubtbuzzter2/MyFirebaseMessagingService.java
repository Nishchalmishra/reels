package arivihan.technologies.doubtbuzzter2;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.enums.NotificationPriority;
import arivihan.technologies.doubtbuzzter2.enums.NotificationType;
import arivihan.technologies.doubtbuzzter2.helper.NewNotificationHelper;
import arivihan.technologies.doubtbuzzter2.helper.firebase.FirebaseMessagingManager;
import arivihan.technologies.doubtbuzzter2.model.GenericNotification;
import arivihan.technologies.doubtbuzzter2.model.NotificationModel;
import arivihan.technologies.doubtbuzzter2.model.NotificationEventMetadata;
import lombok.NonNull;

import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "NOTIFICATION";
    private static final String PAYLOAD_TAG = "FCM_PAYLOAD";


    private static final String RC_DAILY_LIMIT = "notification_daily_limit";
    private static final String RC_GENERIC_DAILY_LIMIT = "generic_notification_daily_limit";
    private static final long DEFAULT_DAILY_LIMIT = 5;



    // [START on_new_token]
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "onNewToken: " + token);
        String storedToken = MyApplication.getDoNotDeleteSharedPrefKey(PreferenceKeys.FCM_TOKEN.getKey());
        if (!token.equals(storedToken)) {
            Log.d(TAG, "onNewToken: token mismatch — updating pref and calling refresh API");
            MyApplication.saveDataForDoNotDeleteSharedPreference(PreferenceKeys.FCM_TOKEN.getKey(), token);
            FirebaseMessagingManager.refreshToken(this, storedToken, token);
        } else {
            Log.d(TAG, "onNewToken: token unchanged, no update needed");
        }
        FirebaseMessagingManager.notifyTokenRefresh(token);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        try {
            logPayload(remoteMessage);

            // Check if notification is from a subscribed topic
            String from = remoteMessage.getFrom();
            if (from != null && from.startsWith("/topics/")) {
                String topic = from.substring(8); // Remove "/topics/" prefix
                if (!isTopicSubscribed(topic)) {
                    Log.d(TAG, "Topic not subscribed: " + topic + " - dropping notification");
                    return;
                }
            }

            //get notification type — sent as "type" or "notificationType"
            Map<String, String> data = remoteMessage.getData();

            // Add FCM message ID and topic to data
            data.put("fcmMessageId", remoteMessage.getMessageId());
            data.put("fcmTopic", from);

            String rawType = NotificationType.rawType(data);
            NotificationType notificationType = NotificationType.from(rawType);
            Log.d(TAG, "type " + rawType + " (" + notificationType + ")");


            // Send receive event for tracking
            NotificationEventMetadata metadata = GenericNotification.buildNotificationMetadata(data);
            EventApiHelper.sendActionEventReceiveNotification(this, metadata);


            //daily cap
            if (limitReached(data, RC_DAILY_LIMIT, PreferenceKeys.NOTIFICATION_DAILY_COUNT.getKey())) {
                EventApiHelper.sendActionEventDropNotification(this, metadata, "DAILY_LIMIT_REACHED");
                return;
            }
            countOne(PreferenceKeys.NOTIFICATION_DAILY_COUNT.getKey()); //count


            //type - new_message
            if (notificationType == NotificationType.NEW_MESSAGE) {
                String senderIdFromFcm = data.get("senderId");

                String batchId = data.get("batchId");
                String groupKey = (batchId != null && !batchId.isEmpty()) ? batchId : "batch_chat";
                int notificationId = senderIdFromFcm != null ? senderIdFromFcm.hashCode() : (int) System.currentTimeMillis();
                String senderName = data.get("senderName");
                String senderType = data.get("senderType");
                String avatarUrl = data.get("avatarUrl");
                String body = data.get("body");

                Log.d(TAG, "Chat notification. notificationId=" + notificationId + ", senderId=" + senderIdFromFcm);
                NotificationHelper.sendChatNotification(this, notificationId, body, senderIdFromFcm, senderName, senderType, avatarUrl, groupKey, "");
                NotificationHelper.showGroupSummary(this, groupKey);
                return;
            }

            //id
            int notificationId = parseNotificationId(data.get("id"));

            //type - ENGAGEMENT
            if (notificationType == NotificationType.ENGAGEMENT) {
                Log.d(TAG, "Data Payload Received: " + data);
                NewNotificationHelper.showFromJson(getApplicationContext(), data, notificationId);
            }

            //type - REGULAR
            else if (notificationType == NotificationType.REGULAR) {
                Log.d(TAG, "REGULAR Notification: Data- " + data.get("body"));
                NotificationHelper.sendNotification(this, data.get("body"));
            }

            //type - open_live_class_countdown
            else if (notificationType == NotificationType.OPEN_LIVE_CLASS_COUNTDOWN) {
                NotificationHelper.sendTimedNotification(this, notificationId, data);
            }

            //type - html_content
            else if (notificationType == NotificationType.HTML_CONTENT) {
                NotificationHelper.sendHtmlNotification(this, notificationId, data);
            }

            //type - GENERIC — unified payload, one renderer for every look
            else if (notificationType == NotificationType.GENERIC) {
                if (limitReached(data, RC_GENERIC_DAILY_LIMIT, PreferenceKeys.GENERIC_NOTIFICATION_DAILY_COUNT.getKey())) {
                    EventApiHelper.sendActionEventDropNotification(this, metadata, "DAILY_LIMIT_REACHED");
                    return;
                }
                countOne(PreferenceKeys.GENERIC_NOTIFICATION_DAILY_COUNT.getKey()); //count

                GenericNotification.show(this, notificationId, data);
            }

            else {
                if (remoteMessage.getNotification() != null) {
                    RemoteMessage.Notification notification = remoteMessage.getNotification();
                    String title = notification.getTitle();
                    String body = notification.getBody();
                    Uri imageUrl = notification.getImageUrl();

                    Log.d(TAG, "Notification Payload Received: Title=" + title + ", Body=" + body + ", ImageUrl=" + imageUrl);

                    String deepLink = data.get("deepLink"); // Deeplink
                    String id = data.get("id");             // Unique id for each notification

                    Log.d(TAG, "DeepLink: " + deepLink + ", NotificationId(raw): " + id);

                    if (deepLink != null && deepLink.equals("app://www.arivihan.com/doubt/solution")) {
                        Log.d(TAG, "Handling Doubt Solved Notification. videoLink=" + data.get("videoLink") + ", doubtId=" + data.get("doubtId"));
                        handleDoubtSolvedNotifiction(notification, data.get("videoLink"), data.get("doubtId"));
                    }

                    if (deepLink != null && deepLink.contains("app://www.arivihan.com/doubt/solution?chatSessionId=")) {
                        Log.d(TAG, "Refreshing Doubt Chat Screen");
                        MyApplication.saveData(PreferenceKeys.REFRESH_DOUBT_CHAT_SCREEN.getKey(), true);
                    }

                    if (deepLink != null && deepLink.equals("app://www.arivihan.com/routine")) {
                        Log.d(TAG, "Handling Routine Notification");
                        handleTodolistNotification();
                    }

                    if (StringUtils.isEmpty(deepLink)) {
                        Log.d(TAG, "DeepLink is empty, setting as blank string");
                        deepLink = "";
                    }

                    String groupKey = null;

                    Log.d(TAG, "Standard notification. Sending via sendRichNotification()");
                    NotificationHelper.sendRichNotification(this,
                            notificationId,
                            body, title, imageUrl, groupKey,
                            NotificationHelper.buildPayloadTapIntent(this, deepLink, data)
                    );

                    NotificationModel notificationModel = new NotificationModel();
                    notificationModel.setNotificationMessage(body);
                    notificationModel.setTitle(title);
                    if (imageUrl != null) notificationModel.setImageUrl(imageUrl.toString());

                } else {
                    // Data-only push — route by notification type
                    String title = data.get("title");
                    String body = data.get("body");

                    if (body == null || body.isEmpty()) {
                        Log.d(TAG, "Data-only message with no body — skipping");
                        return;
                    }

                    boolean sticky = "true".equalsIgnoreCase(data.get("sticky"));
                    Intent tapIntent = NotificationHelper.buildTapIntent(this, rawType, data);
                    NotificationHelper.sendNotification(
                            this,
                            notificationId, body, title != null ? title : "",
                            tapIntent, sticky
                    );
                }
            }
            Log.d(TAG, "onMessageReceived completed");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }







    // ── Daily notification caps ───────────────────────────────────────────────
    private static boolean limitReached(Map<String, String> data, String rcKey, String prefKey) {
        long limit = MyApplication.getFirebaseRemoteConfig().getLong(rcKey);
        if (limit == 0) limit = DEFAULT_DAILY_LIMIT;
        int shown = shownToday(prefKey);
        if (limit < 0 || shown < limit || NotificationPriority.from(data.get("priority")) == NotificationPriority.HIGH) return false;

        Log.d(TAG, rcKey + "=" + limit + " reached (" + shown + " shown) — dropping notification");
        return true;
    }

    private static int shownToday(String prefKey) {
        String stored = MyApplication.getStringData(prefKey);
        String prefix = today() + ":";
        return stored != null && stored.startsWith(prefix) ? Integer.parseInt(stored.substring(prefix.length())) : 0;
    }

    private static void countOne(String prefKey) {
        MyApplication.saveData(prefKey, today() + ":" + (shownToday(prefKey) + 1));
    }

    private static String today() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }







    //parse notification id
    private static int parseNotificationId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return (int) System.currentTimeMillis();
        }

        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return (int) System.currentTimeMillis();
        }
    }


    private void handleDoubtSolvedNotifiction(RemoteMessage.Notification notification, String videoLink, String doubtId) {
        MyApplication.saveData(PreferenceKeys.IS_DOUBT_SOLUTION_READY.getKey(), "yes");
        MyApplication.saveData(PreferenceKeys.DOUBT_NOTIFICATION_TITLE.getKey(), notification.getTitle());
        MyApplication.saveData(PreferenceKeys.DOUBT_NOTIFICATION_VIDEO_LINK.getKey(), videoLink);
        Log.i("CHATSESSIONID", "FIREBASE ID:: " + doubtId);
        MyApplication.saveData(PreferenceKeys.DOUBT_NOTIFICATION_DOUBT_ID.getKey(), doubtId);
    }

    private void handleTodolistNotification() {
        Log.i("TODOLISTREADY ::: ", "TODOLIST READY MESSAGE");
        MyApplication.saveData(PreferenceKeys.IS_TODOLIST_READY.getKey(), true);
    }


//    public void updateFcmTokenAndVersion(String deviceToken) {
//        String versionName = "";
//        try {
//            Log.d("updateFcmToken", deviceToken);
//            versionName = getPackageManager()
//                    .getPackageInfo(getPackageName(), 0).versionName;
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("fcmToken", deviceToken);
//            map.put("version", versionName);
//            MyJsonObjectRequest myJsonObjectRequest2 = new MyJsonObjectRequest(Request.Method.GET,
//                    CommonMethods.getUrlParam(SAVE_FCM_TOKEN, map), null,
//                    response -> {
//                        Log.d("updateFcmToken", "success");
//                        MyApplication.saveData(PreferenceKeys.FCM_NOTIFICATION_TOKEN.getKey(), deviceToken);
//                        MyApplication.saveData(PreferenceKeys.FCM_NOTIFICATION_TOKEN_STORE_TIME.getKey(), System.currentTimeMillis() + 86400000);
//                    }, new VolleyGlobalErrorListener(this) {
//                @Override
//                public void postError(VolleyError error) {
//
//                }
//            });
//            MyVolleySingleton.getInstance(this).addToRequestQueue(myJsonObjectRequest2);
//        } catch (Exception e) {
//
//        }
//    }


    // ── Tap-intent factory ────────────────────────────────────────────────────


    // Overload for data-only pushes with a pre-built tap intent
//    private void sendNotification(int notificationId, String messageBody, String messageTitle,
//                                  Uri imageUrl, String deepLink, String groupKey,
//                                  Intent tapIntent) {
//        sendNotification(notificationId, messageBody, messageTitle, imageUrl,
//                deepLink, groupKey, tapIntent, false);
//    }


    /// //// LOG s //////
    // Verify if topic is in tracked subscribed topics
    private boolean isTopicSubscribed(String topic) {
        return FirebaseMessagingManager.getTrackedSubscribedTopics().contains(topic);
    }


    // ── Logging ───────────────────────────────────────────────────────────────
    private void logPayload(RemoteMessage remoteMessage) {
        if (!BuildConfig.DEBUG) return;

        try {
            Log.d(PAYLOAD_TAG, "───── FCM message received ─────");
            Log.d(PAYLOAD_TAG, "messageId=" + remoteMessage.getMessageId()
                    + ", from=" + remoteMessage.getFrom()
                    + ", to=" + remoteMessage.getTo()
                    + ", senderId=" + remoteMessage.getSenderId()
                    + ", messageType=" + remoteMessage.getMessageType()
                    + ", collapseKey=" + remoteMessage.getCollapseKey()
                    + ", sentTime=" + remoteMessage.getSentTime()
                    + ", ttl=" + remoteMessage.getTtl()
                    + ", priority=" + remoteMessage.getPriority()
                    + ", originalPriority=" + remoteMessage.getOriginalPriority());

            RemoteMessage.Notification notification = remoteMessage.getNotification();
            if (notification != null) {
                Log.d(PAYLOAD_TAG, "notification: title=" + notification.getTitle()
                        + ", body=" + notification.getBody()
                        + ", imageUrl=" + notification.getImageUrl()
                        + ", channelId=" + notification.getChannelId()
                        + ", clickAction=" + notification.getClickAction()
                        + ", sound=" + notification.getSound()
                        + ", tag=" + notification.getTag());
            } else {
                Log.d(PAYLOAD_TAG, "notification: <none — data-only message>");
            }

            Map<String, String> data = remoteMessage.getData();
            if (data.isEmpty()) {
                Log.d(PAYLOAD_TAG, "data: <empty>");
            } else {
                Log.d(PAYLOAD_TAG, "data: " + data.size() + " key(s)");
                // One line per key so long payloads aren't cut off by logcat's per-entry limit
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    Log.d(PAYLOAD_TAG, "  " + entry.getKey() + " = " + entry.getValue());
                }
            }

            logRawBundle(remoteMessage);
        } catch (Exception e) {
            Log.w(PAYLOAD_TAG, "Failed to log FCM payload", e);
        }
    }


    @SuppressWarnings("deprecation")
    private void logRawBundle(RemoteMessage remoteMessage) {
        Bundle raw = remoteMessage.toIntent().getExtras();
        if (raw == null || raw.isEmpty()) {
            Log.d(PAYLOAD_TAG, "raw bundle: <empty>");
        } else {
            Log.d(PAYLOAD_TAG, "raw bundle: " + raw.size() + " key(s)");
            for (String key : raw.keySet()) {
                Object value = raw.get(key);
                Log.d(PAYLOAD_TAG, "  " + key + " = " + value
                        + (value == null ? "" : "   [" + value.getClass().getSimpleName() + "]"));
            }
        }

        byte[] rawData = remoteMessage.getRawData();
        Log.d(PAYLOAD_TAG, "rawData: " + (rawData == null ? "<none>" : rawData.length + " bytes"));
    }




    public static boolean isNotificationShowing(Context context, int notificationId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return true;
        }

        for (android.service.notification.StatusBarNotification notification : manager.getActiveNotifications()) {
            if (notification.getId() == notificationId) {
                return true;
            }
        }
        return false;
    }



}