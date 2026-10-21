package arivihan.technologies.doubtbuzzter2.enums;

public enum TodayClassType {
    VIDEO(0),
    PRACTICE_QUESTION(1),
    ASAT(2),
    NOTES(3),
    TEST(4),
    DYNAMIC(5),
    ENGAGEMENT(6),
    DYNAMIC2(7),
    DYNAMIC_VIDEO(8),
    LIVE_CLASSES(9),
    VIDEO_NAVIGATION(10),
    CONTINUE_LECTURE(11),
    DPP_LIVE(12),
    DPP_PRACTICE(13);
    private final int type;

    TodayClassType(int index) {
        this.type = index;
    }

    public int type() {
        return type;
    }
}
