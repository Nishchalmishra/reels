package arivihan.technologies.doubtbuzzter2.enums.competitive;

public enum AdaptivePath {
    REVIEW_TEST_SERIES("review-test_series"),
    TEST_SERIES_INSTRUCTIONS("test-series-instructions"),
    TOPIC_WISE("topic-wise"),
    DPP("dpp"),
    DPP_GUIDELINES("dpp/guidelines"),
    DPP_PRACTICE("dpp/practice"),

    VIRTUAL_BATCH("virtual-batch"),

    ADAPTIVE_PRACTISE_FALSE("test/{level}/{topicId}/false"),
    MICROLECTURE_PRACTISE("chapterwise-practice"),
    LIVE_CLASS("live-class");

    private final String path;
    AdaptivePath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
