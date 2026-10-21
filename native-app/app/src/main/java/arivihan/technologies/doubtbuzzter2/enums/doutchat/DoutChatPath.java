package arivihan.technologies.doubtbuzzter2.enums.doutchat;

public enum DoutChatPath {
    ASK_DOUBT("ask-doubt"),
    DOUBT_HISTORY("doubt-history"),
    RESUME_SESSION("doubt-chat/{chatSessionId}"),
    LECTURE_DOUBT("contextual"),
    MOCK_TEST_DOUBT("mock-test-doubt"),
    PSEUDO_LIVE("pseudo-live"),
    CQNQ("ask-more"),
    PDF_CIRCLE("pdf-circle");

    private final String path;

    DoutChatPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}