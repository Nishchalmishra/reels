package arivihan.technologies.doubtbuzzter2.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserScore {
    String message;
    String userScore;
    double precisionUserScore;
    String totalScore;
    String cutOff;
    String userRank;
    String totalUser;
}
