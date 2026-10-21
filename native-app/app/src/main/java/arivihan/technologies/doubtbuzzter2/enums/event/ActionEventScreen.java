package arivihan.technologies.doubtbuzzter2.enums.event;

/**
 * Stable screen-name constants for the {@code screenName} param of
 * {@link arivihan.technologies.doubtbuzzter2.volley.EventApiHelper#sendActionEvent}.
 * Always send one of these — never a class/fragment name, which gets
 * ProGuard-minified in release builds and breaks reports grouped by screen.
 */
public enum ActionEventScreen {
    EXAM_SCREEN("EXAM_SCREEN"),
    PHONE_SCREEN("PHONE_SCREEN"),
    OTP_SCREEN("OTP_SCREEN"),
    MEDIUM_SCREEN("MEDIUM_SCREEN"),
    CLASS_SCREEN("CLASS_SCREEN"),
    STREAM_SCREEN("STREAM_SCREEN"),
    NAME_SCREEN("NAME_SCREEN"),
    PHONE_NUMBER("PHONE_NUMBER"),
    OTP("OTP"),
    NEET_CLASS_SCREEN("NEET_CLASS_SCREEN"),
    NEET_CLASS_UNAVAILABLE_SCREEN("NEET_CLASS_UNAVAILABLE_SCREEN"),
    NEET_COMING_SOON_SCREEN("NEET_COMING_SOON_SCREEN");

    private final String value;

    ActionEventScreen(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
