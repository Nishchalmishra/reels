package arivihan.technologies.doubtbuzzter2.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VideoResponse {
    private Integer id;
    private String code;
    @SerializedName("url")
    private String videoUrl;
    @SerializedName("thumbnailUrl")
    private String imageUrl;
    private String orientation;
private String context;
    private String type;
    private Integer displayOrder;
    private Boolean isActive;
}
