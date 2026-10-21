package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedback implements Serializable {

    private String contentId;
    private String microlectureId;
    private boolean liked;
    private String comment;
    private List<String> feedbacks;
    private String chapterId;
    private ContentFeedbackType feedbackType;

}
