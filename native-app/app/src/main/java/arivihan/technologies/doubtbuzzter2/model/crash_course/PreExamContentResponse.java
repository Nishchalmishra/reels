package arivihan.technologies.doubtbuzzter2.model.crash_course;

import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PreExamContentResponse {
    private String title;
    private String content;
    private String contentType;
    private Map<String, Object> activityParams;
    private String navigationType;
    private String className;
    private String iconType;
    private String categoryId;

    public PreExamContentResponse() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Map<String, Object> getActivityParams() { return activityParams; }
    public void setActivityParams(Map<String, Object> activityParams) { this.activityParams = activityParams; }

    public String getNavigationType() { return navigationType; }
    public void setNavigationType(String navigationType) { this.navigationType = navigationType; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
}
