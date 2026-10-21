package arivihan.technologies.doubtbuzzter2.enums.event;

/**
 * Stable event-name constants for {@link arivihan.technologies.doubtbuzzter2.volley.EventApiHelper#sendActionEvent}
 * calls in the onboarding/login funnel. Client-owned events only.
 */
public enum ActionEventName {
    APP_OPENED("APP_OPENED"),
    SCREEN_VIEWED("SCREEN_VIEWED"),
    EXAM_SELECTED("EXAM_SELECTED"),
    AUTH_INITIATED("AUTH_INITIATED"),
    MEDIUM_SCREEN_VIEWED("MEDIUM_SCREEN_VIEWED"),
    MEDIUM_SELECTED("MEDIUM_SELECTED"),
    CLASS_SCREEN_VIEWED("CLASS_SCREEN_VIEWED"),
    CLASS_SELECTED("CLASS_SELECTED"),
    STREAM_SCREEN_VIEWED("STREAM_SCREEN_VIEWED"),
    STREAM_SELECTED("STREAM_SELECTED"),
    NAME_SCREEN_VIEWED("NAME_SCREEN_VIEWED"),
    NAME_SUBMITTED("NAME_SUBMITTED"),
    EDIT_PHONE_NUMBER_CLICK("EDIT_PHONE_NUMBER_CLICK"),
    RESEND_OTP_CLICK("RESEND_OTP_CLICK"),
    CONTACT_US_CLICK("CONTACT_US_CLICK"),
    NEET_CLASS_SCREEN_VIEWED("NEET_CLASS_SCREEN_VIEWED"),
    NEET_CLASS_SELECTED("NEET_CLASS_SELECTED"),
    NEET_CLASS_UNAVAILABLE_SCREEN_VIEWED("NEET_CLASS_UNAVAILABLE_SCREEN_VIEWED"),
    NEET_BOARD_SELECTED("NEET_BOARD_SELECTED"),
    NEET_COMING_SOON_SCREEN_VIEWED("NEET_COMING_SOON_SCREEN_VIEWED"),
    RESUME_FLOW_INITIATED("RESUME_FLOW_INITIATED");

    private final String value;

    ActionEventName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
