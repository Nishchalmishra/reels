package arivihan.technologies.doubtbuzzter2.model;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProgress {

    private String title;
    private String color;
    private String textColor;
    private List<PointDataList> pointDataList;
    private List<String> xaxisLegends;
}
