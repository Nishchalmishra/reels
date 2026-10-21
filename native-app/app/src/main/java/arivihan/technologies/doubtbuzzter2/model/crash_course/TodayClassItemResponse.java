package arivihan.technologies.doubtbuzzter2.model.crash_course;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.HalfYearlyContentType;
import arivihan.technologies.doubtbuzzter2.model.LiveClassStatus;
import arivihan.technologies.doubtbuzzter2.model.NotesDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodayClassItemResponse {

    private HalfYearlyContentType contentType;

    // Core
    private String type;
    private int displayOrder;
    private int position;
    private boolean locked;
    private Boolean isCompleted;
    private boolean demoLecture;
    private String routineId;
    private Double progress;
    private SubscriptionLockPopupDto subscriptionLockPopupDto;

    // Chapter identifiers
    private String chapterId;
    private String chapterCode;
    private String subjectId;

    // Chapter info
    private String chapterName;
    private String chapterNameHindi;
    private String chapterThumbnailUrl;
    private String chapterType;
    private String chapterStatus;
    private ChapterLiveDto chapterLiveDto;
    private Map<String, Object> liveNavigationBundle;

    // Subject
    private String subject;
    private String subjectIconUrl;

    // Microlecture
    private String microLectureId;
    private String microLectureTitle;
    private String microLectureLanguageTitle;
    private Integer microLectureViewCount;
    private String microLectureViewCountStr;
    private String viewCountText;
    private String thumbnailUrl;
    private Integer duration;
    private String videoDuration;
    private String videoUrl;

    // Live class
    private String liveClassMeetingLink;
    private String liveClassDate;
    private String liveClassDuration;
    private LiveClassStatus liveClassStatus;
    private String recordedLectureUrl;
    private String liveClassSnackBarMessage;

    // UI / Navigation
    private String title;
    private String subTitle;
    private String heading;
    private String btnText;
    private String icon;
    private String deeplink;
    private String notesPdfUrl;
    private String className;
    private String navigationType;
    private Map<String, Object> activityParams;
    private NotesDto notesData;

    // Misc
    private Integer numberOfQuestions;

    private String subjectName;
    private String teacherName;
    private boolean newContent;

    // DPP Fields
    private String dppBatchId;
    private String zoomWebhookUrl;
    private String dppStatus; // LIVE, UPCOMING
    private String meetLink; // DPP's Google Meet link, only when Meet mode is enabled
    private boolean zoomEnabled; // live-meeting provider flags, set on DPP LIVE cards only

    // Batch related
    private boolean batchOverViewLecture;
    private long skipVideoPosition;
    private String itemTag;
    private String rescheduledDate ;
    private String rescheduledStartTime;

    public boolean getIsCompleted() {
        if(isCompleted!=null) {
            return isCompleted;
        }
        return false;
    }

//    public double getProgress() {
//        if(progress!=null) {
//            return progress;
//        }
//        return 0;
//    }
}
