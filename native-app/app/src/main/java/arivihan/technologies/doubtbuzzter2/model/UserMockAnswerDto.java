package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserMockAnswerDto implements Serializable {
    private String listId;
    private String listName;
    private String testName;
    private String id;
    private long t;
    private Map<String, QAnsInfo> ansMap;
    private int numberOfSavAttempted;
    private boolean show;
    private String asatId;
    private String scholarshipInfoId;
    private boolean board;
}
