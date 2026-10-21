package arivihan.technologies.doubtbuzzter2.model.competitive.continue_learning;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.competitive.subject.CompetitiveSubjectDto;
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
public class ContinueLearningDataDto {
    private String title;
    private List<ContinueLearningDto> items;
}