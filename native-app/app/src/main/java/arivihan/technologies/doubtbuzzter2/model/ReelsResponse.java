package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReelsResponse {
    private Long id;
    private String reelsUrl;
    private String thumbnailUrl;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer viewCount;
    private Integer commentCount;
    private Integer bookmarkCount;
    private Integer shareCount;
    private String reelTitle;
    private Integer durationInSeconds;
    private Boolean isGlobal;
}
