package arivihan.technologies.doubtbuzzter2.model;

import arivihan.technologies.doubtbuzzter2.model.call_me_back.CallMeBackModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CarouselDto implements Serializable {
    private String url;
    private String actionDeeplink;
    private CallMeBackModel callMeBack;
    private String className;
    private Map<String, Object> activityParams;
}
