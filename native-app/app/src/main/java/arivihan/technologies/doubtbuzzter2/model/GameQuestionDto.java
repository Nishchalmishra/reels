package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class GameQuestionDto {

    private String questionId;
    private String questionText;
    private Boolean locked;
    private String  questionImageUrl;
    private String answer;
    private String explanation;
    private String questionType;
    private String difficultyLevel;
    private List<String> qoptions;

}
