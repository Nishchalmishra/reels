package arivihan.technologies.doubtbuzzter2.model;

import android.util.Log;

public enum PlanLevel {
    COURSE("Course", "कोर्स"),
    TEST_SERIES("Test Series", "टेस्ट सीरीज़"),
    DOUBT("Doubt", "डाउट"),
    DOUBT_SOLVING("Doubt Solving", "डाउट समाधान"),
    CHAPTER("", ""),
    PARTIAL_COURSE("", ""),
    SUBJECT("", ""),
    HINDI_ENGLISH_COURSE("English + Hindi", "अंग्रेज़ी + हिंदी");

    private final String title;
    private final String titleHindi;

    PlanLevel(String title, String titleHindi) {
        this.title = title;
        this.titleHindi = titleHindi;
    }

    public String getTitle() {
        return title;
    }

    public String getHindiTitle() {
        return titleHindi;
    }


    public static PlanLevel fromString(String value) {
        if (value == null) return null;

        for (PlanLevel level : PlanLevel.values()) {
            if (level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }

        Log.d("PlanLevel", "Unknown PlanLevel: " + value);
        return null;
    }

    public String getLocalizedTitle(String language) {
        return "hi".equalsIgnoreCase(language) ? getHindiTitle() : getTitle();
    }
}
