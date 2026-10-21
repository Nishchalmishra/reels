package arivihan.technologies.doubtbuzzter2.model.competitive.subject;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
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
public class AdaptiveChapters implements Serializable {
    private String subject;
    private String subjectId;
    private String subjectDisplayName;

    private List<NameAndIdDto> chapters;
}
