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
public class TestSeriesWarningConfig implements Serializable {
    @SerializedName("title")
    private String title;

    @SerializedName("subTitle")
    private String subTitle;

    @SerializedName("imgUrl")
    private String imgUrl;

    @SerializedName("loaderTime")
    private long loaderTime;
}
