package arivihan.technologies.doubtbuzzter2.model;

import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterPopUpDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduledLectureStatusDto {

    /** Unique lecture / session id — used for Firebase cleanup */
    private String chapterId;

    /** Bold heading e.g. "Lecture rescheduled!!" */
    private String title;

    /** Smaller grey line e.g. "Aapka yeh lecture reschedule ho gya hai.." */
    private String subTitle;

    /** Remote URL or local resource name for the lecture thumbnail */
    private String thumbnailUrl;

    /** New scheduled start time in epoch-millis (optional, for display) */
    private long newStartTime;

    private String subjectId;
    private String subjectCode;

    private ChapterPopUpDto upcomingStatusPopUp;
}
