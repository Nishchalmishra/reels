package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class QuestionsSubmissionDto {

    private String answer;
    private String currentLevelId;
    private String questionId;
    private long timeTaken;
    private String userId;

}
