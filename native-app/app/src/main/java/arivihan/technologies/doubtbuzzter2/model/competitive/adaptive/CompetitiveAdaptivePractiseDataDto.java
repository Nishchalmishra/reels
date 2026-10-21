package arivihan.technologies.doubtbuzzter2.model.competitive.adaptive;

import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.model.competitive.test_series.CompetitiveTestSeriesDto;
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
public class CompetitiveAdaptivePractiseDataDto {
    private String title;
    private String imageUrl;
    private String className;
    private Map<String, Object> activityParams;
}