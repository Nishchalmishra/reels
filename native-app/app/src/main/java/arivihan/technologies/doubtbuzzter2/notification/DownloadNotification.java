package arivihan.technologies.doubtbuzzter2.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public final class DownloadNotification {
    public static final String CHANNEL_ID = "my_downloads";

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= 26) {
            var mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            var ch = new NotificationChannel(CHANNEL_ID, "My Downloads",
                    NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Note download progress");
            mgr.createNotificationChannel(ch);
        }
    }

    public static NotificationCompat.Builder base(Context ctx, String title) {
        return new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }
}
