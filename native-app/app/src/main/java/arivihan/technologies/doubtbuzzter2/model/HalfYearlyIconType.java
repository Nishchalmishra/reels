package arivihan.technologies.doubtbuzzter2.model;

public enum HalfYearlyIconType {
    VIDEO, TEST_SERIES, NOTES, TIME_TABLE, PHYSICS_NOTES,
    CHEMISTRY_NOTES, BIOLOGY_NOTES, MATHEMATICS_NOTES, CHAPTER,
    CHEMISTRY_SUBJECT, PHYSICS_SUBJECT, BIOLOGY_SUBJECT, MATHEMATICS_SUBJECT,
    HINDI_SUBJECT, ENGLISH_SUBJECT, CHAPTER_LIST, NOTES_LIST, ALL_TEST_SERIES, PRACTICE, ALL_NOTES_LIST,
    ECONOMICS_SUBJECT,
    HISTORY_SUBJECT,
    POLITICAL_SCIENCE_SUBJECT,
    GEOGRAPHY_SUBJECT, ACCOUNTANCY_SUBJECT, BUSINESS_STUDIES_SUBJECT, SOCIOLOGY_SUBJECT, ASK_DOUBT;

    public static HalfYearlyIconType fromString(String value) {
        if (value == null) return NOTES;
        try {
            return HalfYearlyIconType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NOTES;
        }
    }
}
