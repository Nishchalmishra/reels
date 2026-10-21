package arivihan.technologies.doubtbuzzter2.model;


import java.util.Map;

import lombok.Data;

@Data
public class HowToDto {
    long timestamp;
    String buttonText;
    String className;
    private Map<String, Object> activityParams;
}
