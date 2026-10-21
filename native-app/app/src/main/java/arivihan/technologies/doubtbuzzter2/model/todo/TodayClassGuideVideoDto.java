package arivihan.technologies.doubtbuzzter2.model.todo;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodayClassGuideVideoDto implements Serializable {
    private String videoUrl;
    private String thumbnail;
    private String identifier;
    private String deepLink;
    private String className;
    private Map<String, Object> activityParams;
}