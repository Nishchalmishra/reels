package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeDislikeResponse implements Serializable {
    private boolean success;
    private String message;
    private long likeCount;
    private Boolean liked;
}
