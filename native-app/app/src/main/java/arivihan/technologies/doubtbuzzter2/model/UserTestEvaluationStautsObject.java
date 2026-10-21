package arivihan.technologies.doubtbuzzter2.model;

import java.util.Date;

import lombok.Data;

@Data
public class UserTestEvaluationStautsObject {
    private String testId;
    private String testName;
    private String listId;
    private String listName;
    private  TestEvaluationStatusEnum testEvaluationStatusEnum;
    private long startTime;
    private long closeTime;
    private long resultIn;
    private Date createdAt;
    private int numberOfSavAttempted;
    private boolean show;
}
