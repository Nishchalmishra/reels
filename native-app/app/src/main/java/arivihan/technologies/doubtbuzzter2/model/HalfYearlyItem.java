package arivihan.technologies.doubtbuzzter2.model;

import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class HalfYearlyItem {
    private HalfYearlyContentType contentType;
    private String title;
    private String subTitle;
    private String microLectureId;
    private String position;
    private String thumbnailUrl;
    private String subjectIconUrl;
    private String microLectureTitle;
    private String microLectureViewCount;
    private String chapterName;
    private String duration;
    private String subject;
    private boolean showTaskCompleteIcon;
    private HalfYearlyTaskStatus routineTaskStatus;
    private String chapterCode;
    private String subjectCode;
    private String content;
    private String contentId;
    private String routineId;
    private Long videoProgress;
    private String chapterId;
    private int numberOfQuestions;
    private String boardCode;
    private HalfYearlyIconType iconType;
    private NotesDto notesData;
    private Boolean isCompleted;

    private String viewCountText;
    private boolean newContent;
    private Double progress;
    private String microLectureViewCountStr;

    private String navigationType;
    private String className;
    private Map<String, Object> activityParams;

    //Live Engagement
    private String liveClassMeetingLink;
    private String liveClassDate;
    private LiveClassStatus liveClassStatus;
    private String recordedLectureUrl;
    private String liveClassDuration;
    private String liveClassSnackBarMessage;
    private boolean demoLecture;

    //Live Classes
//    private String chapterCode;
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
