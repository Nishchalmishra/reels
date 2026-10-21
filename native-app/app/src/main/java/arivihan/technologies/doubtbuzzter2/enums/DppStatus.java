package arivihan.technologies.doubtbuzzter2.enums;

public enum DppStatus {
    LIVE,
    UPCOMING;

    public static DppStatus fromString(String value) {
        if (value == null) return null;
        try {
            return DppStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
