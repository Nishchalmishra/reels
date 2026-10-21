package arivihan.technologies.doubtbuzzter2.model;

import android.os.Bundle;
import java.io.Serializable;

public class NotificationEventMetadata implements Serializable {

    private static final String PREFIX = "notif_meta_";

    public String notificationId;
    public String notificationKey;
    public String fcmMessageId;
    public String deliveryMode;
    public String fcmTopic;
    public String notificationType;
    public String ctaType;
    public String ctaValue;
    public String title;
    public String body;
    public String priority;
    public String redirectKey;
    public boolean showCountdown;
    public boolean isHtmlContent;
    public boolean sticky;

    public NotificationEventMetadata() {}

    public NotificationEventMetadata(String notificationId, String notificationKey, String fcmMessageId,
                                    String deliveryMode, String fcmTopic,
                                    String notificationType, String ctaType, String ctaValue,
                                    String title, String body, String priority,
                                    String redirectKey, boolean showCountdown, boolean isHtmlContent, boolean sticky) {
        this.notificationId = notificationId;
        this.notificationKey = notificationKey;
        this.fcmMessageId = fcmMessageId;
        this.deliveryMode = deliveryMode;
        this.fcmTopic = fcmTopic;
        this.notificationType = notificationType;
        this.ctaType = ctaType;
        this.ctaValue = ctaValue;
        this.title = title;
        this.body = body;
        this.priority = priority;
        this.redirectKey = redirectKey;
        this.showCountdown = showCountdown;
        this.isHtmlContent = isHtmlContent;
        this.sticky = sticky;
    }

    public void putInBundle(Bundle bundle) {
        bundle.putString(PREFIX + "notificationId", notificationId);
        bundle.putString(PREFIX + "notificationKey", notificationKey);
        bundle.putString(PREFIX + "fcmMessageId", fcmMessageId);
        bundle.putString(PREFIX + "deliveryMode", deliveryMode);
        bundle.putString(PREFIX + "fcmTopic", fcmTopic);
        bundle.putString(PREFIX + "notificationType", notificationType);
        bundle.putString(PREFIX + "ctaType", ctaType);
        bundle.putString(PREFIX + "ctaValue", ctaValue);
        bundle.putString(PREFIX + "title", title);
        bundle.putString(PREFIX + "body", body);
        bundle.putString(PREFIX + "priority", priority);
        bundle.putString(PREFIX + "redirectKey", redirectKey);
        bundle.putBoolean(PREFIX + "showCountdown", showCountdown);
        bundle.putBoolean(PREFIX + "isHtmlContent", isHtmlContent);
        bundle.putBoolean(PREFIX + "sticky", sticky);
    }

    public static NotificationEventMetadata fromBundle(Bundle bundle) {
        if (bundle == null) return new NotificationEventMetadata();

        return new NotificationEventMetadata(
            bundle.getString(PREFIX + "notificationId", ""),
            bundle.getString(PREFIX + "notificationKey", ""),
            bundle.getString(PREFIX + "fcmMessageId", ""),
            bundle.getString(PREFIX + "deliveryMode", ""),
            bundle.getString(PREFIX + "fcmTopic", ""),
            bundle.getString(PREFIX + "notificationType", ""),
            bundle.getString(PREFIX + "ctaType", ""),
            bundle.getString(PREFIX + "ctaValue", ""),
            bundle.getString(PREFIX + "title", ""),
            bundle.getString(PREFIX + "body", ""),
            bundle.getString(PREFIX + "priority", ""),
            bundle.getString(PREFIX + "redirectKey", ""),
            bundle.getBoolean(PREFIX + "showCountdown", false),
            bundle.getBoolean(PREFIX + "isHtmlContent", false),
            bundle.getBoolean(PREFIX + "sticky", false)
        );
    }

}
