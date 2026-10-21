package arivihan.technologies.doubtbuzzter2.enums;

import lombok.Getter;

@Getter
public enum NotificationEventType {
    NOTIFICATION_RECEIVED("RECEIVED"),
    NOTIFICATION_DISMISS("DISMISS"),
    NOTIFICATION_DROP("DROPPED"),
    NOTIFICATION_CLICK("CLICKED");

    private final String value;

    NotificationEventType(String value) {
        this.value = value;
    }

    public static NotificationEventType from(String value) {
        if (value == null) return NOTIFICATION_CLICK;
        for (NotificationEventType type : NotificationEventType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return NOTIFICATION_CLICK;
    }
}
