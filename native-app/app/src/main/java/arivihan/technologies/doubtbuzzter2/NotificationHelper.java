package arivihan.technologies.doubtbuzzter2;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.activity.DoubtChatScreen;
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen;
import arivihan.technologies.doubtbuzzter2.activity.WebviewDoubtChatScreen;
import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;
import arivihan.technologies.doubtbuzzter2.helper.NewNotificationHelper;
import arivihan.technologies.doubtbuzzter2.model.NotificationDto;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";


    private static final ExecutorService CHAT_EXECUTOR = Executors.newSingleThreadExecutor();

    static final Map<Integer, NotificationCompat.MessagingStyle> messagingStyleCache = new HashMap<>();


    // string to DTO
    public static NotificationDto prepareNotification(String data) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(data, NotificationDto.class);
        } catch (Exception e) {
            return null;
        }
    }






    //REGULAR
    public static void sendNotification(Context context, String data) {

        //check data
        NotificationDto dto = NotificationHelper.prepareNotification(data);
        if (dto == null) return;

        //no permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //on click
//        Intent intent = new Intent(context, NotificationHandlerActivity.class);
        Intent intent = new Intent(context, HomeScreen.class);
        intent.putExtra("data", data);

        //image url
        String imageUrl = dto.getImageUrl();
        Uri imageUri = (imageUrl != null && !imageUrl.isEmpty()) ? Uri.parse(imageUrl) : null;

        //notification
        sendRichNotification(context, dto.getNotificationId(), dto.getMessageBody(), dto.getMessageTitle(),
                imageUri, dto.getGroupId(), intent);

        //group
        NotificationCompat.Builder summary = new NotificationCompat.Builder(context, NewNotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.magiclogo_min)
                .setStyle(new NotificationCompat.InboxStyle().setSummaryText(dto.getGroupTitle()))
                .setGroup(dto.getGroupId())
                .setContentText("New messages")
                .setGroupSummary(true)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(dto.getGroupId().hashCode(), summary.build());
    }









    //generic data-only push — plain title/body, optional tap intent, sticky = cannot be swiped away
    public static void sendNotification(Context context,
                                        int notificationId, String messageBody, String messageTitle,
                                        Intent tapIntent, boolean sticky
    ) {
        PendingIntent pendingIntent = null;
        if (tapIntent != null) {
            int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
            pendingIntent = PendingIntent.getActivity(context, notificationId, tapIntent, flags);
        }

        String channelId = NewNotificationHelper.CHANNEL_ID;
        NewNotificationHelper.ensureChannel(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.magiclogo_min)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setOngoing(sticky)
                .setDefaults(Notification.DEFAULT_ALL);
        if (pendingIntent != null) builder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, builder.build());
    }









    //notification-payload / REGULAR push — big text, optional big picture via Picasso, optional group
    public static void sendRichNotification(Context context,
                                            int notificationId, String messageBody, String messageTitle,
                                            Uri imageUrl, String groupKey, Intent tapIntent) {

        //no route resolved — land the user on the home screen
        if (tapIntent == null) {
            tapIntent = new Intent(context, HomeScreen.class);
            tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Build.VERSION.SDK_INT >= 34) {
                pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT | PendingIntent.FLAG_ONE_SHOT);
            } else {
                pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
            }
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, tapIntent, PendingIntent.FLAG_MUTABLE);
        }

        //channel
        NewNotificationHelper.ensureChannel(context);

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NewNotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.drawable.magiclogo_min)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setContentIntent(pendingIntent);

        if (groupKey != null) builder.setGroup(groupKey);

        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //no image — post right away
        if (imageUrl == null || StringUtils.isEmpty(imageUrl.toString())) {
            manager.notify(notificationId, builder.build());
            return;
        }

        //Picasso must be called from the main thread
        new Handler(Looper.getMainLooper()).post(() -> Picasso.get()
                .load(imageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                        manager.notify(notificationId, builder.build());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, final Drawable errorDrawable) {
                        //post the text-only notification anyway
                        manager.notify(notificationId, builder.build());
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                    }
                }));
    }


    //notification-payload tap — className takes priority (backend can drive any screen), else the deepLink
    public static Intent buildPayloadTapIntent(Context context, String deepLink, Map<String, String> data) {
        String className = data.get("className");
        Intent intent;

        // className takes priority — lets backend drive any screen directly
        if (className != null && !className.isEmpty()) {
            try {
                String activityParams = data.get("activityParams");
                JSONObject paramsJson = (activityParams != null && !activityParams.isEmpty()) ? new JSONObject(activityParams) : null;
                intent = NewNotificationHelper.buildTargetIntent(context, className, paramsJson);
            } catch (Exception e) {
                Log.e(TAG, "buildTargetIntent failed, falling back to HomeScreen", e);
                intent = new Intent(context, HomeScreen.class);
            }
        } else {
            intent = new Intent();
            try {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(deepLink));
            } catch (NullPointerException e) {
                intent = new Intent(context, HomeScreen.class);
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }







    //SEND ChatNotification
    public static void sendChatNotification(Context context,
            int notificationId, String messageBody,
                                      String senderId, String senderName,
                                      String senderType, String avatarUrl,
                                      String groupKey, String deepLink) {
        final Context appContext = context.getApplicationContext();
        final String pkg = context.getPackageName();

        // Build the tap intent now (main thread, Firebase is ready) so the URL is
        // embedded in the PendingIntent — no Firebase dependency at tap time.
        Intent tapIntent = getChatNotificationIntent(appContext);

        int piFlags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        final PendingIntent tapPendingIntent = PendingIntent.getActivity(appContext, notificationId, tapIntent, piFlags);

        CHAT_EXECUTOR.execute(() -> {
            NewNotificationHelper.ensureChatChannel(appContext);
            NotificationManager nm = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

            boolean isOfficial = "official".equals(senderType);
            String displayName = isOfficial ? "Arivihan_Official" : (senderName != null && !senderName.isEmpty() ? senderName : senderId);

            Person.Builder personBuilder = new Person.Builder()
                    .setName(displayName)
                    .setKey(senderId);

            if (isOfficial) {
                personBuilder.setIcon(IconCompat.createWithResource(appContext, R.drawable.magiclogo_min));
            } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Bitmap avatar = downloadBitmap(avatarUrl);
                if (avatar != null) {
                    personBuilder.setIcon(IconCompat.createWithBitmap(avatar));
                }
            }
            Person sender = personBuilder.build();
            Person me = new Person.Builder().setName("Me").build();

            NotificationCompat.MessagingStyle messagingStyle = messagingStyleCache.get(notificationId);
            if (messagingStyle == null) {
                messagingStyle = new NotificationCompat.MessagingStyle(me)
                        .setConversationTitle(displayName)
                        .setGroupConversation(false);
                messagingStyleCache.put(notificationId, messagingStyle);
            }
            messagingStyle.addMessage(messageBody, System.currentTimeMillis(), sender);

            Intent deleteIntent = new Intent(appContext, NotificationDismissReceiver.class);
            deleteIntent.putExtra(NotificationDismissReceiver.EXTRA_NOTIFICATION_ID, notificationId);
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(appContext, notificationId,
                    deleteIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, NewNotificationHelper.CHAT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.magiclogo_min)
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + pkg + "/" + R.raw.notification))
                    .setStyle(messagingStyle)
                    .setGroup(groupKey)
                    .setDeleteIntent(deletePendingIntent)
                    .setContentIntent(tapPendingIntent);

            nm.notify(notificationId, builder.build());
        });
    }

    @NonNull
    private static Intent getChatNotificationIntent(Context appContext) {
        String resolvedUrl = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.VIRTUAL_BATCH.path();

        Bundle tapExtras = new Bundle();
        tapExtras.putString("url", resolvedUrl);
        tapExtras.putBoolean("appendUserId", true);
        tapExtras.putBoolean("appendToken", true);
        tapExtras.putBoolean("appendClassId", false);
        tapExtras.putBoolean("appendCourseId", false);
        tapExtras.putBoolean("appendBoard", false);
        tapExtras.putBoolean("isBackRestricted", false);
        tapExtras.putBoolean("appendUserName", true);
        tapExtras.putBoolean("appendUserGender", false);
        tapExtras.putBoolean("allowOrientation", false);
        tapExtras.putBoolean("appendUserImageUrl", false);
        tapExtras.putString("screenName", "VIRTUAL_CHAT");

        Intent tapIntent = new Intent(appContext, WebViewByUrlActivity.class);
        tapIntent.putExtras(tapExtras);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return tapIntent;
    }








    //Send Time Notification
    public static void sendTimedNotification(Context context, int notificationId, Map<String, String> data) {
        final Context appCtx = context.getApplicationContext();
        final String pkg         = context.getPackageName();
        final String title       = data.get("title");
        final String miniTitle   = data.get("miniTitle");
        final String body        = data.get("body");
        final String thumbUrl    = data.get("chapterThumbnail");
        final String startTimeStr = data.get("classStartTime");



        long classStartMs = 0;
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            try {
                classStartMs = Long.parseLong(startTimeStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "sendTimedNotification: invalid classStartTime=" + startTimeStr);
            }
        }


        final long remainingMs = classStartMs - System.currentTimeMillis();
        final long chronometerBase = android.os.SystemClock.elapsedRealtime() + remainingMs;

        NewNotificationHelper.ensureChannel(appCtx);
        final NotificationManager manager = (NotificationManager) appCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (remainingMs <= 0) {
            Log.d(TAG, "sendTimedNotification: countdown already elapsed (remainingMs=" + remainingMs + ") — cancelling id=" + notificationId);
            manager.cancel(notificationId);
            return;
        }

        final Intent tapIntent = buildLiveClassIntent(context, data);
        final PendingIntent tapPi = PendingIntent.getActivity(appCtx, notificationId, tapIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        CHAT_EXECUTOR.execute(() -> {
            try {
                // ── Collapsed RemoteViews ───────────────────────────────────
                RemoteViews small = new RemoteViews(pkg, R.layout.notification_timer_collapsed);

                small.setTextViewText(R.id.txtTimerTitle, title != null ? title : "");
                if (miniTitle != null && !miniTitle.isEmpty()) {
//                    small.setTextViewText(R.id.txtTimerMiniTitle, miniTitle);
//                    small.setViewVisibility(R.id.txtTimerMiniTitle, View.VISIBLE);
                }
                // Countdown Chronometer in the red badge
                small.setChronometer(R.id.chronometer, chronometerBase, null, true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    small.setChronometerCountDown(R.id.chronometer, true);
                }

                // ── Expanded RemoteViews ────────────────────────────────────
                RemoteViews big = new RemoteViews(pkg, R.layout.notification_timer_expanded);

                big.setTextViewText(R.id.txtTimerExpandedTitle, title != null ? title : "");
                if (miniTitle != null && !miniTitle.isEmpty()) {
                    big.setTextViewText(R.id.txtTimerExpandedMiniTitle, miniTitle);
                    big.setViewVisibility(R.id.txtTimerExpandedMiniTitle, View.VISIBLE);
                }
                if (body != null && !body.isEmpty()) {
                    big.setTextViewText(R.id.txtTimerExpandedBody, body);
                    big.setViewVisibility(R.id.txtTimerExpandedBody, View.VISIBLE);
                }
                // Same countdown Chronometer in the expanded "LIVE in" row
                big.setChronometer(R.id.chronometer, chronometerBase, null, true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    big.setChronometerCountDown(R.id.chronometer, true);
                }

                // ── Thumbnail (network I/O — must be here in the executor) ─
                if (thumbUrl != null && thumbUrl.startsWith("http")) {
                    Bitmap thumb = downloadBitmap(thumbUrl);
                    if (thumb != null) {
                        big.setImageViewBitmap(R.id.imgTimerThumbnailLeft, thumb);
                        big.setViewVisibility(R.id.imgTimerThumbnailLeft, View.VISIBLE);
                    }
                }

                // ── Build & post notification ──────────────────────────────
                // setOngoing(true)  → cannot be swiped away
                // setAutoCancel(true) → tap opens screen and dismisses it
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(appCtx, NewNotificationHelper.CHANNEL_ID)
                                .setSmallIcon(R.drawable.magiclogo_min)
                                .setContentTitle(title)
                                .setContentText(body)
                                .setOngoing(true)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setContentIntent(tapPi)
                                .setCustomContentView(small)
                                .setCustomBigContentView(big)
                                .setTimeoutAfter(remainingMs);

                manager.notify(notificationId, builder.build());
                Log.d(TAG, "Timer notification posted id=" + notificationId + ", dismissing in " + remainingMs + "ms");

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> manager.cancel(notificationId), remainingMs);
                }

            } catch (Exception e) {
                Log.e(TAG, "sendTimedNotification failed — check layout IDs and drawable", e);
            }
        });
    }







    //html_content — HTML-formatted title/body, optional big picture, tap routed like open_screen
    public static void sendHtmlNotification(Context context, int notificationId, Map<String, String> data) {
        final Context appCtx = context.getApplicationContext();
        final String title    = data.get("title");
        final String body     = data.get("body");
        final String imgUrl   = data.get("imageUrl");
        final String className      = data.get("className");
        final boolean sticky  = "true".equalsIgnoreCase(data.get("sticky"));


        // html_content routes via className/activityParams — same as open_screen.
        // buildTapIntent("html_content") returns null (no case for it); use open_screen instead.
        final Intent tapIntent;
        if (className != null && !className.isEmpty()) {
            tapIntent = buildTapIntent(context, "open_screen", data);
        } else {
            tapIntent = null;
        }

        final PendingIntent tapPi = (tapIntent != null)
                ? PendingIntent.getActivity(appCtx, notificationId, tapIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
                : null;

        final CharSequence titleSpanned = title == null ? "" : HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT);
        final CharSequence bodySpanned = body == null ? "" : HtmlCompat.fromHtml(body, HtmlCompat.FROM_HTML_MODE_COMPACT);

        NewNotificationHelper.ensureChannel(appCtx);
        final NotificationManager nm = (NotificationManager) appCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(appCtx, NewNotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.drawable.magiclogo_min)
                        .setContentTitle(titleSpanned)
                        .setContentText(bodySpanned)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(bodySpanned))
                        .setAutoCancel(true)
                        .setOngoing(sticky)
                        .setDefaults(Notification.DEFAULT_ALL);
        if (tapPi != null) builder.setContentIntent(tapPi);

        if (imgUrl != null && imgUrl.startsWith("http")) {
            CHAT_EXECUTOR.execute(() -> {
                try {
                    Bitmap bmp = downloadBitmap(imgUrl);
                    if (bmp != null) {
                        builder.setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(bmp)
                                .setSummaryText(bodySpanned));
                    }
                    nm.notify(notificationId, builder.build());
                } catch (Exception e) {
                    Log.e(TAG, "sendHtmlNotification (with image) failed", e);
                    nm.notify(notificationId, builder.build());
                }
            });
        } else {
            nm.notify(notificationId, builder.build());
        }
    }










    //////// BUILD TAP INTENT /////

    //maps notificationType to the screen a tap should open — null means no tap action
    public static Intent buildTapIntent(Context context, String notificationType, Map<String, String> data) {
        if (notificationType == null) notificationType = "";
        switch (notificationType) {

            case "open_virtual_batch": {
                return getVirtualBatchIntent(context);
            }

            case "open_live_class", "open_live_class_countdown":
                return buildLiveClassIntent(context, data);

            case "open_doubt_chat": {
                String chatSessionId = data.get("chatSessionId");
                boolean redirectFull = MyApplication.getFirebaseRemoteConfig().getBoolean("doubtchat_redirect_to_full_webview");
                return getOpneDoubtChatIntent(context, redirectFull, chatSessionId);
            }

            case "open_screen": {
                String className = data.get("className");
                String activityParams = data.get("activityParams");
                try {
                    JSONObject paramsJson = (activityParams != null && !activityParams.isEmpty()) ? new JSONObject(activityParams) : null;
                    Intent i = NewNotificationHelper.buildTargetIntent(context, className, paramsJson);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    return i;
                } catch (Exception e) {
                    Log.e(TAG, "open_screen buildTargetIntent failed", e);
                    return null;
                }
            }

            case "do_nothing":

            default:
                return null;
        }
    }

    //open_doubt_chat — native or full-webview doubt chat, chosen by the remote-config flag
    @NonNull
    private static Intent getOpneDoubtChatIntent(Context context, boolean redirectFull, String chatSessionId) {
        Class<?> cls = redirectFull ? WebviewDoubtChatScreen.class : DoubtChatScreen.class;
        Intent i = new Intent(context, cls);
        i.putExtra("chatSessionId", chatSessionId);
        i.putExtra("allowBack", true);
        i.putExtra("openOldChat", false);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    //open_virtual_batch — virtual batch webview with the standard append-user extras
    @NonNull
    private static Intent getVirtualBatchIntent(Context context) {
        String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.VIRTUAL_BATCH.path();
        Bundle extras = new Bundle();
        extras.putString("url", url);
        extras.putBoolean("appendUserId", true);
        extras.putBoolean("appendToken", true);
        extras.putBoolean("appendUserName", true);
        extras.putBoolean("appendUserGender", false);
        extras.putBoolean("allowOrientation", false);
        extras.putBoolean("appendUserImageUrl", false);
        extras.putBoolean("isBackRestricted", false);
        extras.putString("screenName", "VIRTUAL_CHAT");
        Intent i = new Intent(context, WebViewByUrlActivity.class);
        i.putExtras(extras);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }


    //open_live_class — live class webview, chapter/subject/faculty fields passed as serializable params
    public static Intent buildLiveClassIntent(Context context, Map<String, String> data) {
        String url = CommonMethods.getPersonalizedPathUrl() + AdaptivePath.LIVE_CLASS.path();
        java.util.HashMap<String, Object> params = new java.util.HashMap<>();
        if (data.get("chapterName")      != null) params.put("chapterName",      data.get("chapterName"));
        if (data.get("subjectName")      != null) params.put("subjectName",      data.get("subjectName"));
        if (data.get("subjectCode")      != null) params.put("subjectCode",      data.get("subjectCode"));
        if (data.get("chapterId")        != null) params.put("chapterId",        data.get("chapterId"));
        if (data.get("chapterCode")      != null) params.put("chapterCode",      data.get("chapterCode"));
        if (data.get("chapterThumbnail") != null) params.put("chapterThumbnail", CommonMethods.encodeUrl(data.get("chapterThumbnail")));
        if (data.get("facultyName")      != null) params.put("facultyName",      data.get("facultyName"));

        Bundle extras = new Bundle();
        extras.putString("url", url);
        extras.putSerializable("params", params);
        extras.putBoolean("appendUserId", true);
        extras.putBoolean("appendToken", true);
        extras.putBoolean("appendUserName", true);
        extras.putBoolean("appendUserGender", true);
        extras.putBoolean("allowOrientation", true);
        extras.putBoolean("appendUserImageUrl", true);
        extras.putBoolean("isBackRestricted", false);
        extras.putString("screenName", "LIVE");
        Intent i = new Intent(context, WebViewByUrlActivity.class);
        i.putExtras(extras);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }



    public static void showGroupSummary(Context context, String groupKey) {
        NewNotificationHelper.ensureChannel(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder summary =
                new NotificationCompat.Builder(context, NewNotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.drawable.magiclogo_min)
                        .setGroup(groupKey)
                        .setGroupSummary(true)
                        .setAutoCancel(true);
        notificationManager.notify(groupKey.hashCode(), summary.build());
    }




    //blocking image fetch for avatars/thumbnails — background thread only, null on any failure (uses Glide caching)
    public static Bitmap downloadBitmap(String urlStr) {
        if (urlStr == null || !urlStr.startsWith("http")) return null;

        try {
            FutureTarget<Bitmap> futureTarget = Glide.with(MyApplication.getInstance())
                    .asBitmap()
                    .load(urlStr)
                    .submit();

            return futureTarget.get();
        } catch (Exception e) {
            Log.w(TAG, "Avatar download failed", e);
            return null;
        }
    }


}