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
public class TestAnalysisConfigModel implements Serializable {
    @SerializedName("ASAT")
    private TestAnalysisConfig asatConfig;
    @SerializedName("DEFAULT")
    private TestAnalysisConfig defaultConfig;
}
