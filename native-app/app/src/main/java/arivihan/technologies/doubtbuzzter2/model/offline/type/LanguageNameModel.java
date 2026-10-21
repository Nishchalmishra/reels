package arivihan.technologies.doubtbuzzter2.model.offline.type;

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
public class LanguageNameModel implements Serializable {
    @SerializedName("en")
    private String en;

    @SerializedName("hi")
    private String hi;
}
