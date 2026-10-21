package arivihan.technologies.doubtbuzzter2.model.microlecture;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class FeedbackModel implements Serializable {
    private String title;
    private String subtitle;
    private String primaryBtnText;
    private String secondaryBtnText;
    private String primarySubText;
    private String secondarySubText;
    private List<FeedbackOption> feedbackOptions;
}
