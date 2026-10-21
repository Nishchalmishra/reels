package arivihan.technologies.doubtbuzzter2.model.remote_config;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.SnackBarMessageModel;
import arivihan.technologies.doubtbuzzter2.model.call_me_back.CallMeBackModel;
import arivihan.technologies.doubtbuzzter2.model.offline.type.LanguageNameModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNavigationConfig implements Serializable {
    @SerializedName("className")
    private String className;

    @SerializedName("params")
    private Map<String, Object> params;

    @SerializedName("featureName")
    private String featureName;

    @SerializedName("featureIcon")
    private String featureIcon;

    @SerializedName("isNew")
    private boolean isNew;

    @SerializedName("callMeBack")
    private CallMeBackModel callMeBack;

    @SerializedName("navigationType")
    private String navigationType;
}
