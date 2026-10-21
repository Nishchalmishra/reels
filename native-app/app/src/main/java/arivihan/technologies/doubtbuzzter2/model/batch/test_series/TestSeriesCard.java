package arivihan.technologies.doubtbuzzter2.model.batch.test_series;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TestSeriesCard implements Serializable {
    private Integer id;
    private String name;
    private String iconUrl;
    private String color;
    private String accessType;
    private String displayKey;
    private Integer displayOrder;
    private Integer totalTestCount;
    private String totalTestCountStr;
    private Integer batchId;
    private String testCategoryId;

}
