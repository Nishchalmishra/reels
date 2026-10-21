package arivihan.technologies.doubtbuzzter2.model;

public enum TestGenerationStatus {
    IN_PROGRESS,
    COMPLETED_UNSEEN,
    COMPLETED_SEEN,
    DISMISSED;

    public static TestGenerationStatus getVal(String s) {
        for (TestGenerationStatus status : values()) {
            if (status.name().equalsIgnoreCase(s)) return status;
        }
        return IN_PROGRESS;
    }
}