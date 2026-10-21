package arivihan.technologies.doubtbuzzter2.model;


import java.util.Date;

import lombok.Data;

@Data
public class WebChatRecord {

    private String sessionId;

    private String userId;

    private String title;

    private Date updatedAt;

    private Date createdAt;

    private boolean resolved;

    private String selectedSubject;

    private boolean imageUploaded;

    private boolean containUnwatchedVideo;

    private String selectedSubjectId;

    private String doubtChatQuestionType; /*TEXT/IMAGE*/

    private String doubtSolutionStatus;

    private String doubtImageUrl;
    private String language;
    private String historyCategory;

    public Date getUpdatedAt() {
        if (this.updatedAt == null) {
            return this.createdAt;
        }
        return updatedAt;
    }
}
