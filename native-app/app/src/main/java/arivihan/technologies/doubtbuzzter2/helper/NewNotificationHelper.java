package arivihan.technologies.doubtbuzzter2.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;

public abstract class NewNotificationHelper {

    private static final String TAG = NewNotificationHelper.class.getSimpleName();
    public static final String CHANNEL_ID = "app_notification_v2";
    public static final String CHANNEL_NAME = "General Notifications";
    public static final String CHAT_CHANNEL_ID = "chat_notification_v2";
    public static final String CHAT_CHANNEL_NAME = "Chat Messages";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();


    private static JSONObject deepParseMapToJson(Map<String, String> map) {
        JSONObject root = new JSONObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                String value = entry.getValue().trim();
                if (value.startsWith("{")) {
                    root.put(entry.getKey(), new JSONObject(value));
                } else if (value.startsWith("[")) {
                    root.put(entry.getKey(), new JSONArray(value));
                } else {
                    root.put(entry.getKey(), value);
                }
            } catch (Exception e) {
                try {
                    root.put(entry.getKey(), entry.getValue());
                } catch (Exception ignored) {
                }
            }
        }
        return root;
    }

    public static void showFromJson(Context context, Map<String, String> jsonString, int notificationId) {
        Log.d(TAG, "showFromJson: " + jsonString);
        EXECUTOR.execute(() -> {
            try {
                JSONObject payload = deepParseMapToJson(jsonString);

                String miniTitle = payload.optString("mini_title", "");
                String miniTitleColor = payload.optString("mini_title_color", "#FF9800");

                String title = payload.optString("title", "Notification");
                String titleColor = payload.optString("title_color", "#212121");

                String message = payload.optString("message", "");
                String messageColor = payload.optString("message_color", "#666666");

                JSONObject collapsedImageObj = payload.optJSONObject("collapsed_image");
                String collapsedImageUrl = collapsedImageObj != null
                        ? collapsedImageObj.optString("image_url", "")
                        : "";
                Bitmap collapsedBitmap = downloadBitmap(collapsedImageUrl);

                JSONObject character = payload.optJSONObject("character");
                String imageUrl = character != null ? character.optString("image_url", "") : "";
                String position = character != null ? character.optString("position", "left") : "left";
                Bitmap expandedBitmap = downloadBitmap(imageUrl);

                // Button block
                JSONObject button = payload.optJSONObject("button");
                String btnText = button != null ? button.optString("text", "") : "";
                String btnTextColor = button != null ? button.optString("text_color", "#FFFFFF") : "#FFFFFF";
                String btnBgColor = button != null ? button.optString("bg_color", "#1E88E5") : "#1E88E5";
                String btnStyle = button != null ? button.optString("style", "flat") : "flat";
                String btnActionUrl = button != null ? button.optString("action_url", "") : "";

                String className = payload.optString("className", "");
                JSONObject activityParams = payload.optJSONObject("activityParams");
                Log.d(TAG, "showFromJson: " + className + "activityParams" + activityParams);


                RemoteViews small = new RemoteViews(context.getPackageName(), R.layout.notification_collapsed);
                RemoteViews big = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);


                setupMiniTitle(small, big, miniTitle, miniTitleColor);
                setupMainContent(small, big, title, titleColor, message, messageColor);

                setupImageCollapsed(small, collapsedBitmap);

                setupImage(big, expandedBitmap, position);

                setupButton(context, big, notificationId, btnText, btnTextColor, btnBgColor, btnStyle, btnActionUrl);

                Intent contentIntent = buildTargetIntent(context, className, activityParams);
                PendingIntent contentPI = PendingIntent.getActivity(
                        context,
                        10 + notificationId,
                        contentIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );

                ensureChannel(context);

                @SuppressLint("ResourceAsColor") Notification notification =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.magiclogo_min)
                                .setAutoCancel(true)
                                .setContentIntent(contentPI)
                                .setColor(ContextCompat.getColor(context, R.color.arivihan_primary_color))
                                .setCustomContentView(small)
                                .setCustomBigContentView(big)
                                .build();
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    NotificationManagerCompat.from(context).notify(notificationId, notification);
                } else {
                    Log.e(TAG, "POST_NOTIFICATIONS permission not granted, cannot show notification");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error creating notification", e);
            }
        });
    }

    /**
     * ---------------- HELPER METHODS ----------------
     */

    public static Intent buildTargetIntent(Context context, String className, JSONObject activityParams) {
        Intent intent;
        try {
            if (className != null && !className.isEmpty()) {
                if(className.equals("external_link")){
                    return new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(activityParams.getString("link")));
                }else {
                    Class<?> targetClass = ActivityLauncher.resolveClass(className);
                    intent = new Intent(context, targetClass);

                    if (activityParams != null) {
                        Iterator<String> keys = activityParams.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            try {
                                Object value = activityParams.get(key);
                                if (value instanceof Integer) {
                                    intent.putExtra(key, (Integer) value);
                                } else if (value instanceof Boolean) {
                                    intent.putExtra(key, (Boolean) value);
                                } else {
                                    intent.putExtra(key, value.toString());
                                }
                            } catch (JSONException e) {
                                Log.w(TAG, "Invalid param for key: " + key, e);
                            }
                        }
                    }
                }
            } else {
                // fallback
                intent = new Intent(context, HomeScreen.class);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Target class not found: " + className, e);
            intent = new Intent(context, HomeScreen.class);
        } catch (JSONException e) {
            Log.e(TAG, "Error in parsing jSON :: " + className, e);
            intent = new Intent(context, HomeScreen.class);
        }
        return intent;
    }

    private static void setupImageCollapsed(RemoteViews small, Bitmap bitmap) {
        if (bitmap == null) {
            small.setViewVisibility(R.id.imgCollapsed, View.GONE);
        } else {
            small.setViewVisibility(R.id.imgCollapsed, View.VISIBLE);
            small.setImageViewBitmap(R.id.imgCollapsed, bitmap);
        }
    }

    private static void setupImage(RemoteViews big, Bitmap bitmap, String position) {
        if (bitmap == null) {
            big.setViewVisibility(R.id.imgLeftLarge, View.GONE);
            big.setViewVisibility(R.id.imgRightLarge, View.GONE);
            return;
        }
        if ("right".equalsIgnoreCase(position)) {
            big.setViewVisibility(R.id.imgRightLarge, View.VISIBLE);
            big.setViewVisibility(R.id.imgLeftLarge, View.GONE);
            big.setImageViewBitmap(R.id.imgRightLarge, bitmap);
        } else {
            big.setViewVisibility(R.id.imgLeftLarge, View.VISIBLE);
            big.setViewVisibility(R.id.imgRightLarge, View.GONE);
            big.setImageViewBitmap(R.id.imgLeftLarge, bitmap);
        }
    }

    private static void setupMiniTitle(RemoteViews small, RemoteViews big, String text, String color) {
        if (text.isEmpty()) return;
        setTextWithColor(small, R.id.txtMiniTitle, text, color, "#FF9800");
        setTextWithColor(big, R.id.txtMiniTitleLarge, text, color, "#FF9800");
    }

    private static void setupMainContent(RemoteViews small, RemoteViews big,
                                         String title, String titleColor, String message, String messageColor) {
        setTextWithColor(small, R.id.txtMain, title, titleColor, "#212121");
        setTextWithColor(big, R.id.txtMainLarge, title, titleColor, "#212121");
        setTextWithColor(big, R.id.txtSecondaryLarge, message, messageColor, "#666666");
    }

    private static void setupButton(Context context, RemoteViews big, int notificationId,
                                    String text, String textColor, String bgColor, String style, String actionUrl) {
        if (text.isEmpty()) return;

        setTextWithColor(big, R.id.btnAction, text, textColor, "#FFFFFF");
        Log.d(TAG, "setupButton style: " + style);

        try {
            if ("rounded".equalsIgnoreCase(style)) {
                big.setInt(R.id.btnAction, "setBackgroundResource", R.drawable.bg_button_rounded);
                ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(bgColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    big.setColorStateList(R.id.btnAction, "setBackgroundTintList", colorStateList);
                }
            } else {
                Log.d(TAG, "Button style set to flat with color: " + bgColor);
                big.setInt(R.id.btnAction, "setBackgroundColor", Color.parseColor(bgColor));
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid button background color: " + bgColor);
            if ("rounded".equalsIgnoreCase(style)) {
                big.setInt(R.id.btnAction, "setBackgroundResource", R.drawable.bg_button_rounded);
                ColorStateList defaultColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.notification_btn_blue));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    big.setColorStateList(R.id.btnAction, "setBackgroundTintList", defaultColor);
                }
            } else {
                big.setInt(R.id.btnAction, "setBackgroundColor", ContextCompat.getColor(context, R.color.notification_btn_blue));
            }
        }
    }

    private static void setTextWithColor(RemoteViews views, int viewId, String text, String color, String fallback) {
        if (text == null || text.trim().isEmpty()) {
            views.setViewVisibility(viewId, View.GONE);
            return;
        }

        views.setTextViewText(viewId, text);
        try {
            views.setTextColor(viewId, Color.parseColor(color));
        } catch (IllegalArgumentException e) {
            views.setTextColor(viewId, Color.parseColor(fallback));
        }
        views.setViewVisibility(viewId, View.VISIBLE);
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }
    }

    public static void ensureChatChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel(
                    CHAT_CHANNEL_ID, CHAT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            );
            channel.setSound(soundUri, audioAttributes);
            nm.createNotificationChannel(channel);
        }
    }

    private static Bitmap downloadBitmap(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) return null;

        try {
            FutureTarget<Bitmap> futureTarget = Glide.with(MyApplication.getInstance())
                    .asBitmap()
                    .load(urlStr)
                    .submit();

            return futureTarget.get();
        } catch (Exception e) {
            Log.w(TAG, "Failed to download image: " + urlStr, e);
            return null;
        }
    }
}
