package arivihan.technologies.doubtbuzzter2.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.call_me_back.CallMeBackModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NavigationConfig implements Serializable {
    private String className;
    private Map<String, Object> params;
    private String featureName;
    private String featureIcon;
    private boolean isNew;
    private String navigationType;
}
