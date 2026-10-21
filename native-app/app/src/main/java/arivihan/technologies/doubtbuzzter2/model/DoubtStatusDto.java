package arivihan.technologies.doubtbuzzter2.model;

import java.util.Date;

import lombok.Data;

@Data
public class DoubtStatusDto {
    String doubtText;
    String sessionId;
    private DoubtSolutionStatus status;
    private long startTime;
    private long closeTime;
    private long resultIn;
    private Date createdAt;
    private String language;
}
