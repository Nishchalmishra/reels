package arivihan.technologies.doubtbuzzter2.enums.ChapterEnum;

public enum ChapterType {
    LIVE_CHAPTER,RECORDED_CHAPTER;

    public static ChapterType fromString(String status) {
        try {
            return ChapterType.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return null; // or a default value
        }
    }
}
