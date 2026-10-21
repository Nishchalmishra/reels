package arivihan.technologies.doubtbuzzter2.model.remote_config.poster;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.call_me_back.CallMeBackModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PosterPopUpConfig implements Serializable {

    @SerializedName("url")
    private String url;

    @SerializedName("className")
    private String className;

    @SerializedName("params")
    private Map<String, Object> params;

    @SerializedName("timer")
    private int timer;

    @SerializedName("callMeBack")
    private CallMeBackModel callMeBack;
}


