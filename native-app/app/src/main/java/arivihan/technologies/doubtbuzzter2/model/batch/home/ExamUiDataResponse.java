package arivihan.technologies.doubtbuzzter2.model.batch.home;

import android.view.View;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamUiDataResponse {

    @SerializedName("examId")
    private Integer examId;

    @SerializedName("examCode")
    private String examCode;

    @SerializedName("examName")
    private String examName;

    @SerializedName("streamName")
    private String streamName;

    @SerializedName("gradeName")
    private String gradeName;

    @SerializedName("iconUrl")
    private String homeScreenTitleIcon;

    @SerializedName("backgroundImageUrl")
    private String homeScreenHeaderBackgroundImageUrl;

    @SerializedName("footerImageUrl")
    private String homeScreenFooterImageUrl;

    @SerializedName("greetingMessage")
    private String homeScreenTitleText;

    @SerializedName("headerTags")
    private List<String> headerTags;

    @SerializedName("profileEditable")
    private Boolean profileEditable;

    @SerializedName("isSelected")
    private boolean isSelected;

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }
}
