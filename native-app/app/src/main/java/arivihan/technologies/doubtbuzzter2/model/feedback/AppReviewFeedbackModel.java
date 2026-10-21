package arivihan.technologies.doubtbuzzter2.model.feedback;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppReviewFeedbackModel implements Serializable {
    private String feedback;
    private String modulesLiked;
    private int stars;
}
