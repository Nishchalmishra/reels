package arivihan.technologies.doubtbuzzter2.model.carousel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CarouselResponse implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("code")
    private String code;

    @SerializedName("screenType")
    private String screenType;

    @SerializedName("visibilityType")
    private String visibilityType;

    @SerializedName("url")
    private String url;

    @SerializedName("batchId")
    private Integer batchId;

    @SerializedName("batchCode")
    private String batchCode;

    @SerializedName("displayOrder")
    private Integer displayOrder;

    @SerializedName("className")
    private String className;

    @SerializedName("activityParams")
    private Map<String, Object> activityParams;

    @SerializedName("navigationType")
    private String navigationType;
}
