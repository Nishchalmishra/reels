package arivihan.technologies.doubtbuzzter2.model.competitive;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureSectionModel {
    private String icon;
    private String title;
    private String className;
    private String activityParams;
    private String navigationType;
    private int position;
    private String tag;
    private String tagBackgroundColor;
    private String iconBackgroundColor;
    private Integer minVersionCode;
    private Long progress;
    private String iconMediaType;

}


