package arivihan.technologies.doubtbuzzter2.model;

import java.util.Map;

import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import lombok.Data;

@Data
public class UiSession {
    private String        id;
    private String        sessionDate;
    private String        title;
    private String        subject;
    private String        subjectColor;
    private String        startTime;
    private String        endTime;
    private String        duration;
    private ChapterStatus status;
    private String        thumbnailUrl;
    private String        teacherInfoImageUrl;
    private String        teacherName;
    private String        streamUrl;
    private ChapterLiveDto chapterLiveDto;
    private Map<String, Object> liveNavigationBundle;
    private boolean       locked;
    private String        liveUrl;
    private boolean rescheduled;
    private String  rescheduledDate;
    private String  rescheduledTime;


    public UiSession(String title, String subject, String subjectColor,
                     String duration, String status, boolean locked,
                     String streamUrl, String liveUrl) {
        this.title        = title;
        this.subject      = subject;
        this.subjectColor = subjectColor;
        this.duration     = duration;
        this.status       = ChapterStatus.fromString(status);
        this.locked       = locked;
        this.streamUrl    = streamUrl;
        this.liveUrl      = liveUrl;
    }

    /** Null-safe convenience accessor. */
    public String getLiveUrl() {
        if (liveUrl != null)        return liveUrl;               // set via constructor
        if (chapterLiveDto != null) return chapterLiveDto.getLiveUrl(); // set via setter
        return "";
    }
}