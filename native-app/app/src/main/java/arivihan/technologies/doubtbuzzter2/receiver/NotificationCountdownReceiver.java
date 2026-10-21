package arivihan.technologies.doubtbuzzter2.receiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyFirebaseMessagingService;
import arivihan.technologies.doubtbuzzter2.model.GenericNotification;

public class NotificationCountdownReceiver extends BroadcastReceiver {

    private static final String TAG = "CountdownReceiver";
    private static final String ACTION_EXPIRE = "arivihan.countdown.EXPIRE";
    private static final String ACTION_CLEAR = "arivihan.countdown.CLEAR";


    public static void scheduleExpiry(Context context, int notificationId, Map<String, String> data, long triggerAtMillis, long clearWindowMs) {
        Intent intent = new Intent(context, NotificationCountdownReceiver.class)
                .setAction(ACTION_EXPIRE)
                .putExtra("notificationId", notificationId)
                .putExtra("data", toJson(data))
                .putExtra("triggerAt", triggerAtMillis)
                .putExtra("clearWindow", clearWindowMs);
        schedule(context, intent, notificationId, triggerAtMillis);
    }

    public static void scheduleClear(Context context, int notificationId, long triggerAtMillis) {
        Intent intent = new Intent(context, NotificationCountdownReceiver.class)
                .setAction(ACTION_CLEAR)
                .putExtra("notificationId", notificationId);
        schedule(context, intent, notificationId, triggerAtMillis);
    }


    public static void cancel(Context context, int notificationId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        for (String action : new String[]{ACTION_EXPIRE, ACTION_CLEAR}) {
            int requestCode = ACTION_CLEAR.equals(action) ? ~notificationId : notificationId;
            Intent intent = new Intent(context, NotificationCountdownReceiver.class).setAction(action);
            PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        int notificationId = intent.getIntExtra("notificationId", 0);

        if (ACTION_CLEAR.equals(intent.getAction())) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.cancel(notificationId);
            Log.d(TAG, "Cleared id=" + notificationId);
            return;
        }

        if (ACTION_EXPIRE.equals(intent.getAction())) {
            long triggerAt = intent.getLongExtra("triggerAt", 0L);
            long clearWindow = intent.getLongExtra("clearWindow", 0L);
            long callbackAtMs = System.currentTimeMillis();
            long lateBy = triggerAt > 0 ? callbackAtMs - triggerAt : 0L;

            if (clearWindow > 0 && lateBy >= clearWindow) return;

            Map<String, String> data = fromJson(intent.getStringExtra("data"));
            if (data.isEmpty()) return;

            // Only update if notification is still showing
            if (!MyFirebaseMessagingService.isNotificationShowing(context, notificationId)) return;


            // Freeze timer and show "live starting soon"
            data.put("bypassCountDown", "true");
            data.put("silent", "true");

            PendingResult pendingResult = goAsync();
            try {
                GenericNotification.show(context, notificationId, data);
                Log.d(TAG, "Updated id=" + notificationId);
            } catch (Exception e) {
                Log.e(TAG, "Failed to update id=" + notificationId, e);
            } finally {
                pendingResult.finish();
            }
        }
    }


    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private static void schedule(Context context, Intent intent, int notificationId, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        long now = System.currentTimeMillis();
        long delayMs = triggerAtMillis - now;

        // Log alarm time set (format as HH:MM:SS)
        String triggerTimeStr = formatTime(triggerAtMillis);
        Log.d(TAG, "⏰ SET id=" + notificationId + " | triggerAt=" + triggerTimeStr + " | delayMs=" + delayMs);

        // Runtime permission check for API 31+ (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted, using window");
            }
        }

        int requestCode = ACTION_CLEAR.equals(intent.getAction()) ? ~notificationId : notificationId;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        try {
            // API 31+: Check if exact alarms can be scheduled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!am.canScheduleExactAlarms()) {
                    am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 1_000L, pi);
                    Log.d(TAG, "⏰ Scheduled (window-1s) id=" + notificationId + ", delayMs=" + delayMs);
                    return;
                }
            }

            // API 23+: Use setExactAndAllowWhileIdle (exact timing, ignores Doze, ~0 tolerance)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            Log.d(TAG, "⏰ Scheduled (exact-<1s) id=" + notificationId + ", delayMs=" + delayMs);

        } catch (SecurityException e) {
            Log.w(TAG, "Permission denied, using setAndAllowWhileIdle: " + e.getMessage());
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule, fallback to 1s window: " + e.getMessage());
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 1_000L, pi);
        }
    }

    private static String toJson(Map<String, String> data) {
        if (data == null || data.isEmpty()) return "";
        try {
            return new JSONObject(data).toString();
        } catch (Exception e) {
            Log.e(TAG, "Failed to serialize data", e);
            return "";
        }
    }

    private static Map<String, String> fromJson(String json) {
        Map<String, String> data = new HashMap<>();
        if (json == null || json.isEmpty()) return data;
        try {
            JSONObject object = new JSONObject(json);
            for (Iterator<String> keys = object.keys(); keys.hasNext(); ) {
                String key = keys.next();
                data.put(key, object.optString(key));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse data", e);
        }
        return data;
    }

    private static String formatTime(long timeMillis) {
        return new SimpleDateFormat("HH:mm:ss", Locale.US).format(timeMillis);
    }
}
