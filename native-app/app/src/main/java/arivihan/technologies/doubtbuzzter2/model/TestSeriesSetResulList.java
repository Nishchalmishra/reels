package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSeriesSetResulList implements Serializable {
    private boolean separate;
    private List<TestSeriesSetDto> testSeriesList;
}
