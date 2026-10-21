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
public class AppFeedbackStatusModel implements Serializable {
    private boolean show_feedback_dialog;
    private boolean review_submitted;
}
