package arivihan.technologies.doubtbuzzter2.model.competitive.test_series;

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
public class CompetitiveTestSeriesDto implements Serializable {
    private String id;
    private String name;
    private String color;
    private String count;
    private String iconUrl;
    private String testCountStr;
}
