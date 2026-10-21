package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SyncDownloadRequest {
    private String microLectureId;
    private String videoUrl;
    private String videoDuration;
    private String videoQuality;
    private long startTime;
    private long endTime;
}
