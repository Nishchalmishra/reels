package arivihan.technologies.doubtbuzzter2.model.todo;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class TodoExamDate implements Serializable {
    private String submittedExamDate;
    private String selectionExamStartDate;
    private String selectionExamEndDate;
    private String examId;
    private String examName;
}
