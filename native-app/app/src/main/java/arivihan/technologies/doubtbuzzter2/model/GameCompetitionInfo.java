package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class GameCompetitionInfo implements Serializable {
    private String gameId;
    private String gameName;
    private GameSyllabus syllabus;
    private List<List<String>> instructions;
    private String prize;
    private long registrationStartDate;
    private long gameStartTime;
    private long gameEndTime;
    private long totalScore;
    private long timeToComplete;
    private boolean participated;
    private long noOfParticipant;
    private String registrationInfo;
    private String termsAndCondition;
    private String header;
}
