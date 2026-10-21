package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.Map;

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
public class SubscriptionCheckDto implements Serializable {
    private String dialogTitle;
    private String dialogText;
    private String className;
    private String buttonText;
    private Map<String, Object> activityParams;

    // Written out instead of relying on @Data: Lombok-generated accessors are invisible to
    // the Kotlin compiler, and the Compose topper-notes lock dialog needs these. Lombok
    // skips generating a method that already exists, so @Data still covers the rest.
    public String getDialogTitle() {
        return dialogTitle;
    }

    public String getDialogText() {
        return dialogText;
    }

    public String getButtonText() {
        return buttonText;
    }

    public Map<String, Object> getActivityParams() {
        return activityParams;
    }
}
