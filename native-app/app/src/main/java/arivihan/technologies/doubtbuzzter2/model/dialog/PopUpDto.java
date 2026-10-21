package arivihan.technologies.doubtbuzzter2.model.dialog;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopUpDto implements Serializable {
    private String title;
    private String subTitle;
    private String btnName;
    private String navigationType;
    private String className;
    private Map<String, Object> activityParams;
}
