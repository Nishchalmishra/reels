package arivihan.technologies.doubtbuzzter2.model.competitive.dpp;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.subject.SubjectResponse;
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
public class DppLearnScreenDataDto {
    private String title;
    private List<SubjectResponse> subjects;
}