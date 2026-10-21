package arivihan.technologies.doubtbuzzter2.model;

public enum DoubtSolutionStatus {
    IN_PROGRESS,
    SOLVED_SEEN,
    SOLVED_UNSEEN;

    public static DoubtSolutionStatus getVal(String s) {
        for (DoubtSolutionStatus doubtSolutionStatus : values()) {
            if (DoubtSolutionStatus.valueOf(s).equals(doubtSolutionStatus)) {
                return doubtSolutionStatus;
            }
        }
        return SOLVED_SEEN;
    }
}