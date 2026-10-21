package arivihan.technologies.doubtbuzzter2.model.batch.home;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BottomSheetContentResponse implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("url")
    private String url;

    @SerializedName("title")
    private String title;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("btnText")
    private String btnText;

    @SerializedName("className")
    private String className;

    @SerializedName("activityParams")
    private String activityParams;

    @SerializedName("forLiveClass")
    private boolean forLiveClass;

    @SerializedName("batchCode")
    private String batchCode;

    @SerializedName("showPopup")
    private boolean showPopup;

    @SerializedName("buttons")
    private List<BottomSheetActionButtonResponse> buttons;
}
