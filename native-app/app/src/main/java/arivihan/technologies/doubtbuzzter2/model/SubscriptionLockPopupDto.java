package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SubscriptionLockPopupDto implements Serializable {
    private String title;
    private String subTitle;
    private String btnName;
    private String className;
    private Map<String, Object> activityParams;
    private List<String> courseIds;
    private long autoCloseDialogTimer;
}
