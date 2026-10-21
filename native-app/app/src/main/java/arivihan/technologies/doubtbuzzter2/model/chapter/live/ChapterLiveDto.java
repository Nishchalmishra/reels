package arivihan.technologies.doubtbuzzter2.model.chapter.live;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterLiveDto implements Serializable {

    private String id;
    private String chapterId;
    private String chapterCode;
    private String liveUrl;
    private Long startTime;
    private Long endTime;
    private Boolean active;
    private String createdAt;
    private String updatedAt;

    private String statusMessage;
    private ChapterPopUpDto upcomingStatusPopUp;
    private String rescheduled;
    private String rescheduledOn;


//    private String status;
//    private String upcomingMessage;
//    private Integer pinnedMessageDuration;
//    private Boolean viewerCountVisible;

}
