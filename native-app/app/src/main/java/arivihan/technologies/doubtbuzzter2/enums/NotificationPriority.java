package arivihan.technologies.doubtbuzzter2.enums;

import androidx.core.app.NotificationCompat;

import lombok.Getter;


@Getter
public enum NotificationPriority {
    LOW(NotificationCompat.PRIORITY_LOW),
    MEDIUM(NotificationCompat.PRIORITY_DEFAULT),
    HIGH(NotificationCompat.PRIORITY_HIGH);

    private final int compatPriority;

    NotificationPriority(int compatPriority) {
        this.compatPriority = compatPriority;
    }

    public static NotificationPriority from(String value) {
        String v = value == null ? "" : value.trim();
        for (NotificationPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(v)) return priority;
        }
        return "2".equals(v) || "3".equals(v) ? HIGH : "0".equals(v) ? LOW : MEDIUM;
    }
}