package arivihan.technologies.doubtbuzzter2.enums;

import java.util.Map;

import lombok.Getter;


@Getter
public enum NotificationType {
    REGULAR("REGULAR"),
    GENERIC("generic"),
    ENGAGEMENT("ENGAGEMENT"),
    NEW_MESSAGE("new_message"),
    OPEN_LIVE_CLASS_COUNTDOWN("open_live_class_countdown"),
    HTML_CONTENT("html_content");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public static String rawType(Map<String, String> data) {
        if (data == null) return "";
        String type = data.get("type");
        if (type == null || type.trim().isEmpty()) type = data.get("notificationType");
        return type == null ? "" : type.trim();
    }

    public static NotificationType from(String value) {
        if (value == null) return null;
        for (NotificationType type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) return type;
        }
        return null;
    }
}