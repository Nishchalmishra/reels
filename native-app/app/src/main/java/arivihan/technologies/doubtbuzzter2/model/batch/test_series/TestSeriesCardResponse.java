package arivihan.technologies.doubtbuzzter2.model.batch.test_series;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestSeriesCardResponse {
    private String title;
    private List<TestSeriesCard> testSeries;
}
