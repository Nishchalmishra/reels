package arivihan.technologies.doubtbuzzter2.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OfflineDownloadEntity {
    private String userId;
    private long startTime;
    private String microLectureId;
    private String url;
    private long duration;
    private String quality;
    private long endTime;
}
