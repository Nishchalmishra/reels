package arivihan.technologies.doubtbuzzter2.enums.ChapterEnum;

public enum ChapterStatus {
    UPCOMING, LIVE, CONVERTING, RECORDED,LOCKED,
    LIVE_SCHEDULED,PENDING,RESCHEDULED,MISSED,
    BACKLOG,COMING_SOON;

    public static ChapterStatus fromString(String status) {
        try {
            return ChapterStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return RECORDED;
        }
    }

}

