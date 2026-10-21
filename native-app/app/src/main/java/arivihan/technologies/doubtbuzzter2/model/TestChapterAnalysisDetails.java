package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestChapterAnalysisDetails {

    private String id;
    private String name;
    private String correctColor;
    private String wrongColor;
    private int correct;
    private int wrong;
    private int total;
    private int totalAttempted;
    private boolean strength;

}
