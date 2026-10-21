package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReelInfoDto {
    private String id;
    private String reelsUrl;
    private long likes;
    private long views;
    private String name;
    private ReelsType reelsType;
    private int aspectRatio;
    private boolean liked;

    @Override
    public String toString() {
        return "ReelInfoDto{" +
                "id='" + id + '\'' +
                ", reelsUrl='" + reelsUrl + '\'' +
                ", likes=" + likes +
                ", liked=" + liked +
                '}';
    }
}
