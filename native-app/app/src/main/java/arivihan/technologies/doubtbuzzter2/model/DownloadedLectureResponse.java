package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DownloadedLectureResponse {
    private String userId;
    private long startTime;
    private String microLectureId;
    private String videoUrl;
    private String videoDuration;
    private String videoQuality;
    private long endTime;
    private boolean locked;
    private boolean deleted;
    private String microLectureTitle;
    private String thumbnail;
}
