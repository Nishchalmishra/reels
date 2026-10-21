package arivihan.technologies.doubtbuzzter2.enums.competitive;

public enum CompetitiveContinueLearningType {
    MICROLECTURE(1),
    ADAPTIVE_PRACTICE(2),
    NOTES(3),
    QUESTION(4),
    TEST(5);

    private final int type;
    CompetitiveContinueLearningType(int index) {
        this.type = index;
    }

    public int type() {
        return type;
    }
}
