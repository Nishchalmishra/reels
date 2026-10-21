package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class CompetitionUserAnswer implements Serializable {
    private String userId;
    private long attemptedOn;
    private String gameId;
    private long timeTaken;
    private List<AnswerInfoList> answerInfoList;
}
