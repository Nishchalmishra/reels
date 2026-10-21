package arivihan.technologies.doubtbuzzter2.model.batch;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBatchDto implements Serializable {
    private Integer id;
    private String name;
    private String code;
    private int displayOrder;
    private String selectedSubjectId;
    private boolean defaultSelection;
    private List<SubjectResponse> subjects;
    private boolean isBatchAddOn;

    // For NEET support
    private String boardCode;
    private String displayName;

}
