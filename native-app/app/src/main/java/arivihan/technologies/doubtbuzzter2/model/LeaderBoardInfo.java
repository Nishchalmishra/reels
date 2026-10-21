package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LeaderBoardInfo implements Serializable {
    private String userId;
    private String userName;
    private long position;
    private long timeTaken;
    private String readableTimeTaken;
    private long scoreObtained;
    private boolean prizeWon;
    private String prize;
    private String prizeDescription;
    private String prizeImage;

}
