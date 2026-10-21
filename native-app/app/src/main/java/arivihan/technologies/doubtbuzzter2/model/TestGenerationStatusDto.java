package arivihan.technologies.doubtbuzzter2.model;

import lombok.Data;

@Data
public class TestGenerationStatusDto {
    private String testName;
    private String requestId;
    private TestGenerationStatus status;
    private long startTime;
    private long closeTime;
}