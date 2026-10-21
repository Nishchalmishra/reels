package arivihan.technologies.doubtbuzzter2.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.NotificationHelper;
import arivihan.technologies.doubtbuzzter2.NotificationDismissReceiver;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.enums.NotificationPriority;
import arivihan.technologies.doubtbuzzter2.enums.NotificationType;
import arivihan.technologies.doubtbuzzter2.enums.ThumbnailPosition;
import arivihan.technologies.doubtbuzzter2.helper.NewNotificationHelper;
import arivihan.technologies.doubtbuzzter2.receiver.NotificationCountdownReceiver;


public class GenericNotification {

    private static final String TAG = "GenericNotification";
    private static final int MAX_ACTIONS = 3;
    private static final String DO_NOTHING = "do_nothing";

    private static final String RC_COUNTDOWN_CLEAR_MINUTES = "notification_countdown_clear_minutes";
    private static final String RC_COUNTDOWN_PRE_ALERT_MS = "notification_countdown_pre_alert_ms";


    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();


    //data
    private int notificationId;
    private String groupId = "", title = "", body = "", miniTitle = "", thumbnailUrl = "";
    private boolean sticky, htmlContent, showCountdown, bypassCountDown;
    private long countdownTime;
    private String activity = "", params = "", redirectKey = "";                  //tap route
    private List<Cta> ctas = Collections.emptyList();
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    private ThumbnailPosition thumbnailPosition = ThumbnailPosition.LEFT;
    private boolean showLiveBadge = true;






    //show notification
    public static void show(
            Context context,
            int notificationId,
            Map<String, String> data
    ) {
        Context appCtx = context.getApplicationContext();

        //extract data
        GenericNotification n = new GenericNotification();
        if (data != null) {
            String notificationIdStr = firstNonEmpty(data.get("notificationKey"), data.get("id"));
            n.notificationId = !isEmpty(notificationIdStr) ? (int) parseLong(notificationIdStr, notificationId) : notificationId;
            n.groupId = str(data.get("groupId"));
            n.title = str(data.get("title"));
            n.body = str(data.get("body"));
            n.miniTitle = str(data.get("miniTitle"));
            n.thumbnailUrl = firstNonEmpty(data.get("thumbnailUrl"), data.get("imageUrl"), data.get("chapterThumbnail"));
            n.sticky = parseBool(data.get("sticky"));

            //html content - Remote Config priority
            try {
                boolean alwaysHtml = MyApplication.getFirebaseRemoteConfig().getBoolean("always_html_in_notification");
                if (alwaysHtml) n.htmlContent = true;
                else n.htmlContent = parseBool(data.get("isHtmlContent"));
            } catch (Exception e) {
                n.htmlContent = parseBool(data.get("isHtmlContent"));
            }

            n.showCountdown = parseBool(data.get("showCountdown"));
            n.showLiveBadge = parseBool(data.get("showLiveBadge"));
            n.bypassCountDown = parseBool(data.get("bypassCountDown"));
            n.priority = NotificationPriority.from(data.get("priority"));
            n.redirectKey = str(data.get("redirectKey"));
//            n.soundType = firstNonEmpty(data.get("soundType"), "default");

            //redirectable
            //showThumbnail


            // countdown time: prioritize countdownTime, then scheduleTime + countdownDuration, then classStartTime
            long countdownTime = parseLong(data.get("countdownTime"), 0L);
            if (countdownTime == 0) {
                long scheduleTime = parseLong(data.get("scheduleTime"), 0L);
                long countdownDuration = parseLong(data.get("countdownDuration"), 0L);

                if (scheduleTime > 0 && countdownDuration > 0) {
                    countdownTime = scheduleTime + countdownDuration;
                } else {
                    countdownTime = parseLong(data.get("classStartTime"), 0L);
                }
            } else {
                Log.d(TAG, "Using direct countdownTime: " + countdownTime);
            }
            n.countdownTime = countdownTime;

           //thumbnail position
            try {
                String pos = str(firstNonEmpty(data.get("thumbnailPosition"), data.get("imagePosition")));
                n.thumbnailPosition = pos.isEmpty() ? ThumbnailPosition.LEFT : ThumbnailPosition.valueOf(pos.toUpperCase());
            } catch (Exception e) {
                n.thumbnailPosition = ThumbnailPosition.LEFT;
            }

            //activity and params
            n.activity = str(data.get("activity"));
            n.params = str(data.get("params"));

            Map<String, String> paramsData = new HashMap<>();
            if (!n.params.isEmpty()) {
                n.params = mergeParams(n.params, paramsData);
            } else {
                n.params = toJsonString(paramsData);
            }

            //ctas
            String json = str(data.get("ctas"));
            n.ctas = new ArrayList<>();

            if (!isEmpty(json)) {
                try {
                    JSONArray array = new JSONArray(json.trim());

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.optJSONObject(i);

                        if (item == null || isEmpty(item.optString("label"))) continue;
                        Cta cta = new Cta();
                        cta.type = item.optString("type");
                        cta.label = item.optString("label");
                        cta.activity = item.optString("activity");
                        cta.params = item.optString("params");
                        n.ctas.add(cta);
                    }

                    n.ctas.sort(Comparator.comparingInt(cta ->
                            "PRIMARY".equalsIgnoreCase(cta.type) ? 0 :
                            "SECONDARY".equalsIgnoreCase(cta.type) ? 1 : 2
                    ));

                } catch (Exception e) {
                    Log.w(TAG, "Invalid ctas json: " + json, e);
                }
            }
        }


        //title check - allow empty body
        if (n.title.isEmpty()) return;

        //permission
        if (ActivityCompat.checkSelfPermission(appCtx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "POST_NOTIFICATIONS not granted — cannot show id=" + n.notificationId);
            return;
        }

        //notification clicks - build metadata early
        NotificationEventMetadata metadata = buildNotificationMetadata(data);

        Intent tapIntent = n.buildActionIntent(appCtx, n.activity, n.params);
        if (tapIntent != null) {
            tapIntent.putExtra("redirectionFrom", "NOTIFICATION");
            Bundle metadataBundle = new Bundle();
            metadata.putInBundle(metadataBundle);
            tapIntent.putExtras(metadataBundle);
        }

        PendingIntent tapPi = tapIntent == null ? null : PendingIntent.getActivity(appCtx, n.notificationId,
                tapIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        List<NotificationCompat.Action> actions = n.buildActions(appCtx, metadata, n.notificationId);

        EXECUTOR.execute(() -> {
            try {
                boolean silentFromData = parseBool(data != null ? data.get("silent") : "false");
                n.post(appCtx, tapPi, actions, data, silentFromData, n.notificationId, metadata);
            } catch (Exception e) {
                Log.e(TAG, "Failed to show id=" + n.notificationId, e);
            }
        });
    }


    @SuppressLint("ScheduleExactAlarm")
    private void post(
            Context appCtx,
            PendingIntent tapPi,
            List<NotificationCompat.Action> actions,
            Map<String, String> sourceData,
            boolean silent,
            int notificationId,
            NotificationEventMetadata metadata
    ) {
        NewNotificationHelper.ensureChannel(appCtx);
        NotificationManager manager = (NotificationManager) appCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence titleHtml = text(title);
        CharSequence bodyHtml = text(body);
        CharSequence titlePlain = stripHtml(title);
        CharSequence bodyPlain = stripHtml(body);

        int defaults = silent ? 0 : Notification.DEFAULT_ALL;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appCtx, NewNotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.magiclogo_min)
                .setContentTitle(titlePlain)
                .setContentText(bodyPlain)
                .setAutoCancel(true)
                .setOngoing(sticky)
                .setPriority(priority.getCompatPriority())
                .setOnlyAlertOnce(silent)
                .setDefaults(defaults);

        //sound will add after
//        applySound(appCtx, builder);

        if (tapPi != null) builder.setContentIntent(tapPi);
        if (!groupId.isEmpty()) builder.setGroup(groupId);
        for (NotificationCompat.Action action : actions) builder.addAction(action);
        Bitmap thumbnail = thumbnailUrl.startsWith("http") ? NotificationHelper.downloadBitmap(thumbnailUrl) : null;

        //dismiss receiver
        Intent dismissIntent = new Intent(appCtx, NotificationDismissReceiver.class);
        dismissIntent.setAction("arivihan.intent.action.NOTIFICATION_DISMISSED");
        dismissIntent.putExtra(NotificationDismissReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        Bundle dismissMetadataBundle = new Bundle();
        metadata.putInBundle(dismissMetadataBundle);
        dismissIntent.putExtras(dismissMetadataBundle);
        PendingIntent dismissPi = PendingIntent.getBroadcast(appCtx, notificationId, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setDeleteIntent(dismissPi);


        //countdown
        long now = System.currentTimeMillis();
        long countDownRemainingTimeMs = countdownTime - now;
        long clearNotificationAfterMs = clearAfterMs();

        long preAlertMs = MyApplication.getFirebaseRemoteConfig().getLong(RC_COUNTDOWN_PRE_ALERT_MS);
        if (preAlertMs <= 0) preAlertMs = 120_000L; // Default 2 minutes

        //the timer sticks around at 00:00 for clearAfterMs, then it is cleared from the list
        boolean countdown = showCountdown && countdownTime > 0;
        boolean ticking = countdown && countDownRemainingTimeMs > 0;

        // bypass for per alerts
        bypassCountDown = bypassCountDown || countDownRemainingTimeMs > 0 && countDownRemainingTimeMs < preAlertMs;

        //always user custom ui
        applyCustomNotificationView(
                appCtx,
                builder,
                titleHtml,
                bodyHtml,
                thumbnail,
                Math.max(countDownRemainingTimeMs, 0),
                countdown,
                ticking,
                thumbnailPosition,
                showLiveBadge,
                bypassCountDown,
                metadata
        );
        builder.setTimeoutAfter(countDownRemainingTimeMs + clearNotificationAfterMs);    //00:00 plus the grace period

        // Use custom notification view if HTML content AND countdown enabled
//        if (customUI) {
//
//        } else if (thumbnail != null) {
//            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(thumbnail).setSummaryText(bodyHtml));
//        } else {
//            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(bodyHtml));
//        }


        manager.notify(notificationId, builder.build());

        if (!groupId.isEmpty()) {
            postGroupSummaryNotificationIfNeeded(appCtx, groupId);
        }

        //expire
        if (showCountdown && ticking) {
            long alarmTime = countdownTime - preAlertMs + 400;
            if (alarmTime > now) {
                NotificationCountdownReceiver.scheduleExpiry(appCtx, notificationId, sourceData, alarmTime, clearNotificationAfterMs);
            }
        } else {
            NotificationCountdownReceiver.cancel(appCtx, notificationId);
        }

        //pre-O has no setTimeoutAfter — clear it by hand once the grace period is over
        if (countdown && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCountdownReceiver.scheduleClear(appCtx, notificationId, Math.max(now, countdownTime + clearNotificationAfterMs));
        }
    }

    private static long clearAfterMs() {
        long minutes = MyApplication.getFirebaseRemoteConfig().getLong(RC_COUNTDOWN_CLEAR_MINUTES);
        return (minutes <= 0 ? 15 : minutes) * 60_000L;
    }



    // Custom notification view with HTML content and countdown timer
    private void applyCustomNotificationView(
            Context appCtx,
            NotificationCompat.Builder builder,
            CharSequence titleText,
            CharSequence bodyText,
            Bitmap thumbnail,
            long remainingMs,
            boolean countdown,
            boolean ticking,
            ThumbnailPosition position,
            boolean showLiveBadgeParam,
            boolean bypassCountDown,
            NotificationEventMetadata metadata
    ) {
        String pkg = appCtx.getPackageName();
        long base = SystemClock.elapsedRealtime() + remainingMs;

        //small layout
        RemoteViews small = new RemoteViews(pkg, R.layout.notification_timer_collapsed);

        try {
            small.setTextViewText(R.id.app_name, miniTitle);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                small.setViewVisibility(R.id.app_name, View.GONE);
                small.setViewPadding(R.id.rootContainerCollapsed, 2, 2, 2, 2);
            }else {
                small.setViewVisibility(R.id.app_name, View.VISIBLE);
            }
        } catch (Exception e) {
            Log.w(TAG, "⚠️ Error setting app name", e);
        }

        small.setCharSequence(R.id.txtTimerTitle, "setText", titleText);

        if (TextUtils.isEmpty(bodyText)) {
            small.setViewVisibility(R.id.txtTimerCollapsedBody, View.GONE);
        } else {
            small.setCharSequence(R.id.txtTimerCollapsedBody, "setText", bodyText);
            small.setViewVisibility(R.id.txtTimerCollapsedBody, View.VISIBLE);
        }

        //timer
        if (!countdown) {
            small.setViewVisibility(R.id.chronometer, View.GONE);
            small.setViewVisibility(R.id.txtTimerExpired, View.GONE);
        } else if (bypassCountDown) {
            small.setViewVisibility(R.id.chronometer, View.GONE);
            small.setViewVisibility(R.id.txtTimerExpired, View.VISIBLE);
            small.setCharSequence(R.id.txtTimerExpired, "setText", "00:00");
        } else if (remainingMs >= 0) {
            small.setViewVisibility(R.id.chronometer, View.VISIBLE);
            small.setViewVisibility(R.id.txtTimerExpired, View.GONE);
            chronometer(small, base, ticking);
        } else {
            small.setViewVisibility(R.id.chronometer, View.GONE);
            small.setViewVisibility(R.id.txtTimerExpired, View.VISIBLE);
        }

        // Show LIVE badge if enabled
        if (showLiveBadgeParam) {
            small.setViewVisibility(R.id.layoutLiveBadge, View.VISIBLE);
        } else {
            small.setViewVisibility(R.id.layoutLiveBadge, View.GONE);
        }

        int expandedLayout = position == ThumbnailPosition.BOTTOM ?
                R.layout.notification_timer_expanded_bottom :
                R.layout.notification_timer_expanded;


        //big layout
        RemoteViews big = new RemoteViews(pkg, expandedLayout);

        try {
            big.setTextViewText(R.id.app_name, miniTitle);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                big.setViewVisibility(R.id.app_name, View.GONE);
                big.setViewPadding(R.id.rootContainerExpanded, 2, 2, 2, 2);
            }
        } catch (Exception e) {
            Log.w(TAG, "⚠️ Error setting app name in expanded", e);
        }

        big.setCharSequence(R.id.txtTimerExpandedTitle, "setText", titleText);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            big.setViewVisibility(R.id.txtTimerExpandedMiniTitle, View.GONE);
        }else {
            big.setViewVisibility(R.id.txtTimerExpandedMiniTitle, View.VISIBLE);
            showText(big, R.id.txtTimerExpandedMiniTitle, miniTitle);
        }

        big.setCharSequence(R.id.txtTimerExpandedBody, "setText", bodyText);
        if (thumbnail != null) {
            if (position == ThumbnailPosition.LEFT) {
                big.setImageViewBitmap(R.id.imgTimerThumbnailLeft, thumbnail);
                big.setViewVisibility(R.id.thumbnailContainerLeft, View.VISIBLE);
            } else if (position == ThumbnailPosition.RIGHT) {
                big.setImageViewBitmap(R.id.imgTimerThumbnailRight, thumbnail);
                big.setViewVisibility(R.id.thumbnailContainerRight, View.VISIBLE);
            } else if (position == ThumbnailPosition.BOTTOM) {
                big.setImageViewBitmap(R.id.imgTimerThumbnailBottom, thumbnail);
                big.setViewVisibility(R.id.thumbnailContainerBottom, View.VISIBLE);
            }
        }

        if (position == ThumbnailPosition.BOTTOM) {
            big.setViewVisibility(R.id.liveText, View.GONE);
        } else {
            big.setViewVisibility(R.id.liveText, View.VISIBLE);
        }

        //timer
        if (!countdown) {
            big.setViewVisibility(R.id.timerLayout, View.GONE);
            big.setViewVisibility(R.id.chronometer, View.GONE);
            big.setViewVisibility(R.id.txtTimerExpired, View.GONE);
        } else if (bypassCountDown) {
            big.setViewVisibility(R.id.timerLayout, View.VISIBLE);
            big.setViewVisibility(R.id.chronometer, View.GONE);
            big.setViewVisibility(R.id.txtTimerExpired, View.VISIBLE);
            small.setCharSequence(R.id.txtTimerExpired, "setText", "00:00");
        } else if (remainingMs > 0) {
            big.setViewVisibility(R.id.timerLayout, View.VISIBLE);
            big.setViewVisibility(R.id.chronometer, View.VISIBLE);
            big.setViewVisibility(R.id.txtTimerExpired, View.GONE);
            chronometer(big, base, ticking);
        } else {
            big.setViewVisibility(R.id.timerLayout, View.VISIBLE);
            big.setViewVisibility(R.id.chronometer, View.GONE);
            big.setViewVisibility(R.id.txtTimerExpired, View.VISIBLE);
        }

        // Show LIVE badge if enabled
        if (showLiveBadgeParam) {
            big.setViewVisibility(R.id.layoutLiveBadge, View.VISIBLE);
        } else {
            big.setViewVisibility(R.id.layoutLiveBadge, View.GONE);
        }

        // Add CTA buttons to expanded view
        addCtaButtonsToRemoteViews(appCtx, big, metadata, notificationId);

        builder
                .setCustomContentView(small)
                .setCustomBigContentView(big);
    }


    //is html content
    private CharSequence text(String value) {
        if (isEmpty(value)) return "";
        return htmlContent ? HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT) : value;
    }

    private CharSequence stripHtml(String value) {
        if (isEmpty(value) || !htmlContent) return value;
        return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT).toString();
    }

    private static void showText(RemoteViews views, int viewId, CharSequence value) {
        if (TextUtils.isEmpty(value)) return;
        views.setTextViewText(viewId, value);
        views.setViewVisibility(viewId, View.VISIBLE);
    }

    //ticks down while the class is ahead, then stops on 00:00 (a Chronometer is a TextView)
    private static void chronometer(RemoteViews views, long base, boolean ticking) {
        views.setChronometer(R.id.chronometer, base, "%s", ticking); // Format: HH:MM:SS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) views.setChronometerCountDown(R.id.chronometer, ticking);
        if (!ticking) views.setTextViewText(R.id.chronometer, "00:00");
    }




    //ctas[] — each button routes through the same activity vocabulary as the tap
    private List<NotificationCompat.Action> buildActions(Context appCtx, NotificationEventMetadata metadata, int notificationId) {
        List<NotificationCompat.Action> actions = new ArrayList<>();
        for (int i = 0; i < ctas.size() && actions.size() < MAX_ACTIONS; i++) {
            Cta cta = ctas.get(i);
            Intent target = buildActionIntent(appCtx, cta.activity, cta.params);
            if (target == null) continue;

            target.putExtra("redirectionFrom", "NOTIFICATION");
            target.putExtra("cta_type", String.valueOf(i));
            target.putExtra("cta_value", cta.label);
            Bundle ctaMetadataBundle = new Bundle();
            metadata.putInBundle(ctaMetadataBundle);
            target.putExtras(ctaMetadataBundle);

            actions.add(new NotificationCompat.Action(0, cta.label,
                    PendingIntent.getActivity(appCtx, notificationId * 10 + i + 1, target,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)));
        }
        return actions;
    }




    private void postGroupSummaryNotificationIfNeeded(Context appCtx, String groupId) {
        try {
            NotificationManager manager = (NotificationManager) appCtx.getSystemService(Context.NOTIFICATION_SERVICE);
            int summaryId = groupId.hashCode();

            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(appCtx, NewNotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.magiclogo_min)
                    .setGroup(groupId)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY);

            summaryBuilder.setContentTitle(groupId);
            summaryBuilder.setContentText("Multiple notifications");

            manager.notify(summaryId, summaryBuilder.build());
            Log.d(TAG, "Posted summary notification for group=" + groupId + ", summaryId=" + summaryId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to post group summary notification", e);
        }
    }


    // ── Routing ───────────────────────────────────────────────────────────────
    private Intent buildActionIntent(Context context, String activity, String params) {
        if (DO_NOTHING.equalsIgnoreCase(activity)) return null;

        Intent homeIntent = new Intent(context, HomeScreen.class);
        homeIntent.putExtra("redirect_activity", activity);
        homeIntent.putExtra("redirect_params", params);
        homeIntent.putExtra("redirect_key", redirectKey);
        homeIntent.putExtra("count_down_end", isCountdownEnded());

        // FLAG_ACTIVITY_SINGLE_TOP: Reuse existing HomeScreen if open, call onNewIntent()
        // Don't recreate activity if already on top of stack
        return homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    private boolean isCountdownEnded() {
        long remainingMs = countdownTime - System.currentTimeMillis();
        return remainingMs <= 0;
    }




    private static String toJsonString(Map<String, String> map) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                Log.w(TAG, "Failed to add key to JSON: " + entry.getKey(), e);
            }
        }
        return json.toString();
    }

    private static String mergeParams(String paramsJson, Map<String, String> spread) {
        try {
            JSONObject json = new JSONObject(paramsJson.trim());
            for (Map.Entry<String, String> entry : spread.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            return json.toString();
        } catch (Exception e) {
            Log.w(TAG, "Failed to merge params", e);
            return paramsJson;
        }
    }

    // Add CTA buttons to RemoteViews expanded notification
    private void addCtaButtonsToRemoteViews(Context appCtx, RemoteViews big, NotificationEventMetadata metadata, int notifId) {
        if (ctas.isEmpty()) return;

        int[] buttonIds = {R.id.ctaButton1, R.id.ctaButton2, R.id.ctaButton3};

        for (int i = 0; i < Math.min(ctas.size(), MAX_ACTIONS); i++) {
            Cta cta = ctas.get(i);
            int buttonId = buttonIds[i];

            big.setTextViewText(buttonId, cta.label);

            big.setViewVisibility(buttonId, View.VISIBLE);

            Intent ctaIntent = buildActionIntent(appCtx, cta.activity, cta.params);
            if (ctaIntent != null) {
                ctaIntent.putExtra("redirectionFrom", "NOTIFICATION");
                ctaIntent.putExtra("cta_type", String.valueOf(i));
                ctaIntent.putExtra("cta_value", cta.label);

                // Add notification metadata
                Bundle ctaMetadataBundle = new Bundle();
                metadata.putInBundle(ctaMetadataBundle);
                ctaIntent.putExtras(ctaMetadataBundle);

                PendingIntent ctaPi = PendingIntent.getActivity(appCtx,
                        notifId * 100 + i, ctaIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                big.setOnClickPendingIntent(buttonId, ctaPi);
            }
        }
    }








    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String str(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonEmpty(String... values) {
        for (String value : values) if (!isEmpty(value)) return value.trim();
        return "";
    }

    private static boolean parseBool(String value) {
        String v = str(value);
        return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(str(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }


    private static class Cta {
        private String type = "", label = "", activity = "", params = "";
    }


    public static NotificationEventMetadata buildNotificationMetadata(Map<String, String> data) {
        if (data == null) return new NotificationEventMetadata();

        String fcmTopic = data.getOrDefault("fcmTopic", "");

        assert fcmTopic != null;
        String deliveryMode = fcmTopic.startsWith("/topics/") ? "TOPIC" : "true".equalsIgnoreCase(data.getOrDefault("isBulk", "false")) ? "BULK" : "DIRECT";

        return new NotificationEventMetadata(
            data.getOrDefault("id", ""),
            data.getOrDefault("notificationKey", ""),
            data.getOrDefault("fcmMessageId", ""),
            deliveryMode,
            fcmTopic,
            NotificationType.rawType(data),
            data.getOrDefault("ctaType", ""),
            data.getOrDefault("ctaValue", ""),
            data.getOrDefault("title", ""),
            data.getOrDefault("body", ""),
            data.getOrDefault("priority", ""),
            data.getOrDefault("redirectKey", ""),
            parseBool(data.getOrDefault("showCountdown", "false")),
            parseBool(data.getOrDefault("isHtmlContent", "false")),
            parseBool(data.getOrDefault("sticky", "false"))
        );
    }

}
