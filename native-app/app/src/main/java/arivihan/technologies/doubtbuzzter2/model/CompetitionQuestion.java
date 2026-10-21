package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class CompetitionQuestion implements Serializable {
    private String gameId;
    private long gameStartTime;
    private long gameEndTime;
    private List<List<String>> instructions;
    private String gameName;
    private List<TestSeriesQuestionDto> testSeriesQuestionDtos;
}
