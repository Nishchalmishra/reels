package arivihan.technologies.doubtbuzzter2.enums.batch.videos;

public enum VideoOrientationType {
    PORTRAIT,
    LANDSCAPE,
    HORIZONTAL,
    REELS,
    VIDEO;

    public static VideoOrientationType fromString(String value) {
        if (value == null) return VIDEO;
        switch (value.toUpperCase()) {
            case "PORTRAIT": return PORTRAIT;
            case "REELS": return REELS;
            case "LANDSCAPE": return LANDSCAPE;
            case "HORIZONTAL": return HORIZONTAL;
            case "VIDEO": return VIDEO;
            default: return VIDEO;
        }
    }
}
