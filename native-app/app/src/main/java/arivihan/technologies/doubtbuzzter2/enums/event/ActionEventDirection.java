package arivihan.technologies.doubtbuzzter2.enums.event;

/**
 * Navigation direction sent as the {@code data} payload of the onboarding
 * step *_SCREEN_VIEWED events, so a report can tell a forward "Next" arrival
 * apart from a backward "Back" arrival at the same screen.
 */
public enum ActionEventDirection {
    FORWARD("FORWARD"),
    BACKWARD("BACKWARD");

    private final String value;

    ActionEventDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
