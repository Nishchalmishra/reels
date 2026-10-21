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
public class MyDoubtPageConfig implements Serializable {
    @SerializedName("searchVisible")
    private boolean searchVisible;

    @SerializedName("filterVisible")
    private boolean filterVisible;

    @SerializedName("filterMarkerVisible")
    private boolean filterMarkerVisible;

    @SerializedName("askDoubtOptionVisible")
    private boolean askDoubtOptionVisible;
}
