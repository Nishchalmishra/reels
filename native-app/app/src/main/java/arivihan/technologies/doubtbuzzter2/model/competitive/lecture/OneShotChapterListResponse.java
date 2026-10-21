package arivihan.technologies.doubtbuzzter2.model.competitive.lecture;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.banner.BannerDto;
import arivihan.technologies.doubtbuzzter2.model.dialog.PopUpOptionsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OneShotChapterListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<NameAndIdDto> subjects;
    private List<ChapterItem> chapters;
    private BannerDto banner;
    private SubscriptionLockPopupDto lockConfig;
    private PopUpOptionsDto warningPopUp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterItem implements Serializable {
        private String chapterId;
        private String chapterCode;
        private String imageUrl;
        private String subject;
        private String videoTitle;
        private String teacherName;
        private boolean newLecture;
        private String priority;
        private Boolean isLocked;
        private boolean completed;
        private boolean board;
        private String teacherIntroImage;
        private String lockMessage;
        private String notesUrl;
        private String type;
        private String chapterType;
        private String chapterStatus;
        private ChapterLiveDto chapterLiveDto;
        private String rescheduledMessage;
        private String streamServerKey;
    }
}