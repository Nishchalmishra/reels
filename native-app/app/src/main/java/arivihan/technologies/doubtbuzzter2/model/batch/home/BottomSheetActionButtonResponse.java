package arivihan.technologies.doubtbuzzter2.model.batch.home;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BottomSheetActionButtonResponse implements Serializable {

    @SerializedName("buttonText")
    private String buttonText;

    @SerializedName("className")
    private String className;

    @SerializedName("activityParams")
    private String activityParams;

    @SerializedName("actionType")
    private String actionType;

    @SerializedName("dialogBannerImage")
    private String dialogBannerImage;

    @SerializedName("dialogTimeout")
    private Long dialogTimeout;

    @SerializedName("dialogBannerText")
    private String dialogBannerText;

    @SerializedName("bgColorCode")
    private String bgColorCode;

    @SerializedName("textColorCode")
    private String textColorCode;
}
