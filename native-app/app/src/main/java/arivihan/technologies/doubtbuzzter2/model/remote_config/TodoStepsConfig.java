package arivihan.technologies.doubtbuzzter2.model.remote_config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoStepsConfig implements Serializable {
    @SerializedName("headerText")
    private String headerText;

    @SerializedName("subHeaderText")
    private String subHeaderText;

    @SerializedName("bodyText")
    private String bodyText;

    @SerializedName("chapterCheckedText")
    private String chapterCheckedText;

    @SerializedName("chapterUnCheckedText")
    private String chapterUnCheckedText;

    @SerializedName("modeText")
    private String modeText;

    @SerializedName("morningTimeText")
    private String morningTimeText;

    @SerializedName("afternoonTimeText")
    private String afternoonTimeText;

    @SerializedName("eveningTimeText")
    private String eveningTimeText;

    @SerializedName("nightTimeText")
    private String nightTimeText;

    @SerializedName("modeMorningText")
    private String modeMorningText;

    @SerializedName("modeNightText")
    private String modeNightText;

}
