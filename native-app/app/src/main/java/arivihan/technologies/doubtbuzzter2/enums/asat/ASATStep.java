package arivihan.technologies.doubtbuzzter2.enums.asat;

public enum ASATStep {
    UPLOAD_MARKSHEET(0),
    COMPLETE_TEST(1),
    TEST_RESULT(2);

    private final int index;

    ASATStep(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
