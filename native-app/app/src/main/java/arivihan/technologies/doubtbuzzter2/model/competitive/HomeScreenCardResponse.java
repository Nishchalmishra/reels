package arivihan.technologies.doubtbuzzter2.model.competitive;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class HomeScreenCardResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("iconBackgroundColor")
    private String iconBackgroundColor;

    @SerializedName("iconMediaType")
    private String iconMediaType;

    @SerializedName("tag")
    private String tag;

    @SerializedName("tagBackgroundColor")
    private String tagBackgroundColor;

    @SerializedName("visibilityType")
    private String visibilityType;

    // Mapped to "icon" name for adapter compatibility (getIcon())
    @SerializedName("iconUrl")
    private String icon;

    @SerializedName("batchId")
    private Integer batchId;

    @SerializedName("batchCode")
    private String batchCode;

    @SerializedName("displayOrder")
    private Integer displayOrder;

    @SerializedName("className")
    private String className;

    @SerializedName("minVersionCode")
    private Integer minVersionCode;

    @SerializedName("activityParams")
    private Map<String, Object> activityParams;

    @SerializedName("navigationType")
    private String navigationType;

    @SerializedName("parameter")
    private Map<String, Object> parameter;

    // Retained for DYNAMIC_MEDIA type backward compatibility (not in new API, defaults to null)
    private Long progress;

    public String getFeatureTag() {
        if (activityParams == null) return null;
        Object tag = activityParams.get("FEATURE_TAG");
        return tag instanceof String ? (String) tag : null;
    }

}
