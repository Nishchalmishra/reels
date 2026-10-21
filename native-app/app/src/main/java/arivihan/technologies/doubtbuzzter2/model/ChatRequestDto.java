package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDto {
    private String chatSessionId;
    private String doubtId;
    private String prompt;
    private Boolean isMockTestDoubt;
    private String mockQuestion;
    private String mockQuestionAnswer;
}
