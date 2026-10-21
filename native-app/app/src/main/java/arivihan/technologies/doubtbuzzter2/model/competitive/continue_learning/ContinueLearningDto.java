package arivihan.technologies.doubtbuzzter2.model.competitive.continue_learning;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContinueLearningDto implements Serializable {

    private String id;

    //MicroLecture
    private String title;
    private String thumbnailUrl;
    private int progressPercentage;
    private int position;
    private String type;
    private String durationText;
    private String microlectureViews;

    //Question
    private String subtitle;
    private String subjectIcon;
    private String btnText;
    private String bodyText;

    // Common parameters
    private boolean showPosition;
    private boolean isNew;
    private boolean isCompleted;
    private String className;
    private Map<String, Object> activityParams;
}
