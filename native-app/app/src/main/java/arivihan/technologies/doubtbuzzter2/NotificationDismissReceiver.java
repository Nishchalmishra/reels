package arivihan.technologies.doubtbuzzter2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import arivihan.technologies.doubtbuzzter2.model.NotificationEventMetadata;
import arivihan.technologies.doubtbuzzter2.receiver.NotificationCountdownReceiver;
import arivihan.technologies.doubtbuzzter2.volley.EventApiHelper;

public class NotificationDismissReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationDismissReceiver";
    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";


    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        if (notificationId != 0) {

            // Send dismiss event with metadata
            sendNotificationDismissEvent(context, intent);

            NotificationHelper.messagingStyleCache.remove(notificationId);
            NotificationCountdownReceiver.cancel(context, notificationId);
        }
    }

    private void sendNotificationDismissEvent(Context context, Intent intent) {
        try {
            NotificationEventMetadata metadata = NotificationEventMetadata.fromBundle(intent.getExtras());
            EventApiHelper.sendActionEventDismissNotification(context, metadata);
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification dismiss event", e);
        }
    }
}
