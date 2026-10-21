package arivihan.technologies.doubtbuzzter2.enums.competitive;

public enum CompetitiveChapterType {
    LIVE,
    PREMIUM;

    /** Returns the string value used in API calls and remote config. */
    public String getType() {
        return name();
    }

    /** Returns the matching enum for a given string, or {@code null} if not found. */
    public static CompetitiveChapterType from(String type) {
        if (type == null) return null;
        for (CompetitiveChapterType value : values()) {
            if (value.name().equalsIgnoreCase(type)) return value;
        }
        return null;
    }
}
