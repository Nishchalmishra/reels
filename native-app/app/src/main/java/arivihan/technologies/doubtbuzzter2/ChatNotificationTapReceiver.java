package arivihan.technologies.doubtbuzzter2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import arivihan.technologies.doubtbuzzter2.activity.web_view.WebViewByUrlActivity;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.enums.competitive.AdaptivePath;

public class ChatNotificationTapReceiver extends BroadcastReceiver {

    public static final String EXTRA_URL = "chat_notification_url";
    private static final String TAG = ChatNotificationTapReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // URL was embedded at notification-build time so we never need Firebase here.
        // If somehow the extra is missing, fall back to resolving it now.
        String url = intent != null ? intent.getStringExtra(EXTRA_URL) : null;
        if (url == null || url.isEmpty()) {
            try {
                url = CommonMethods.getPersonalizedPathUrl()
                        + AdaptivePath.VIRTUAL_BATCH.path();
            } catch (Exception e) {
                Log.e(TAG, "Failed to resolve virtual chat URL", e);
                return;
            }
        }

        Bundle extras = new Bundle();
        extras.putString("url", url);
        extras.putBoolean("appendUserId", true);
        extras.putBoolean("appendToken", true);
        extras.putBoolean("appendClassId", false);
        extras.putBoolean("appendCourseId", false);
        extras.putBoolean("appendBoard", false);
        extras.putBoolean("isBackRestricted", false);
        extras.putBoolean("appendUserName", true);
        extras.putBoolean("appendUserGender", false);
        extras.putBoolean("allowOrientation", false);
        extras.putBoolean("appendUserImageUrl", false);
        extras.putString("screenName", "VIRTUAL_CHAT");

        Intent launchIntent = new Intent(context, WebViewByUrlActivity.class);
        launchIntent.putExtras(extras);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(launchIntent);
    }
}
