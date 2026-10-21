package arivihan.technologies.doubtbuzzter2.model.competitive.subject;

import java.io.Serializable;

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
public class CompetitiveSubjectDto implements Serializable {
    private String id;
    private String subjectName;
    private String chapterCount;
    private String iconUrl;
}
