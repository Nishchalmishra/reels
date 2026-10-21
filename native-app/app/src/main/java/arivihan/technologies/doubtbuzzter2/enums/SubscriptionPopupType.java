package arivihan.technologies.doubtbuzzter2.enums;

public enum SubscriptionPopupType {
    LECTURES("lectures"),
    NOTES("notes"),
    PYQ("pyq"),
    CHAT("virtual_chat"),
    PARIKSHA_ADHYAYAN("pariksha_adhyayan"),
    UNKNOWN("unknown");

    private final String value;

    SubscriptionPopupType(String value) { this.value = value; }

    public String getValue() { return value; }
}
