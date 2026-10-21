package arivihan.technologies.doubtbuzzter2.model;

public enum HalfYearlyContentType {
    ASAT, VIDEO, TEST_SERIES, NOTES, TIME_TABLE, PHYSICS_NOTES,
    CHEMISTRY_NOTES, BIOLOGY_NOTES, MATHEMATICS_NOTES, CHAPTER,
    CHEMISTRY_SUBJECT, PHYSICS_SUBJECT, BIOLOGY_SUBJECT, MATHEMATICS_SUBJECT,
    ECONOMICS_SUBJECT,
    HISTORY_SUBJECT,
    POLITICAL_SCIENCE_SUBJECT,
    GEOGRAPHY_SUBJECT, ACCOUNTANCY_SUBJECT, BUSINESS_STUDIES_SUBJECT,
    HINDI_SUBJECT, ENGLISH_SUBJECT, CHAPTER_LIST, NOTES_LIST, CHAPTER_LIST_NEW,
    ALL_TEST_SERIES,
    PRACTICE, ALL_NOTES_LIST,
    ASK_DOUBT, LIVE_CLASSES, CLASS_PARAMS, MICROLECTURE_VIDEO, CONTINUE_LECTURE;

    public static HalfYearlyContentType fromString(String value) {
        if (value == null) return CLASS_PARAMS;
        try {
            return HalfYearlyContentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CLASS_PARAMS;
        }
    }
}
