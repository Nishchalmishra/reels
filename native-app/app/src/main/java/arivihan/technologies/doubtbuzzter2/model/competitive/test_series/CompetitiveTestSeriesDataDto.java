package arivihan.technologies.doubtbuzzter2.model.competitive.test_series;

import java.util.List;

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
public class CompetitiveTestSeriesDataDto {
    private String title;
    private List<CompetitiveTestSeriesDto> testList;
}