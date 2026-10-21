package arivihan.technologies.doubtbuzzter2.model;


import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class TodayClassDto {
    private String chapterId;
    private String microLectureId;
    private Integer position;
    private String thumbnailUrl;
    private String subjectIconUrl;
    private String microLectureTitle;
    private String chapterName;
    private Integer duration;
    private String type;
    private Integer numberOfQuestions;
    private Boolean isCompleted;
    private Double progress;
    private Integer microLectureViewCount;
    private String microLectureViewCountStr;
    private boolean newContent;
    private String notesPdfUrl;
    private String viewCountText;
    private String videoDuration;

    //ASAT
    private String heading;
    private String title;
    private String subTitle;
    private String btnText;
    private String icon;
    private String deeplink;

    protected String navigationType;
    private String className;
    private Map<String, Object> activityParams;

    //Routine
    private String routineId;

    //Live Engagement
    private String liveClassMeetingLink;
    private String liveClassDate;
    private LiveClassStatus liveClassStatus;
    private String recordedLectureUrl;
    private String liveClassDuration;
    private String liveClassSnackBarMessage;
    private NotesDto notesData;
    private boolean demoLecture;

    //Live Classes
    private String chapterCode;
    private String subjectId;
    private String subjectName;
    private String chapterType;
    private String chapterStatus;
    private ChapterLiveDto chapterLiveDto;
    private String chapterThumbnailUrl;
    private String  teacherName;
    private boolean locked;
    private SubscriptionLockPopupDto subscriptionLockPopupDto;
    private Map<String, Object> liveNavigationBundle;
}
