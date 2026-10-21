package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class LeaderBoard implements Serializable {
    private String gameId;
    private String gameName;
    private LeaderBoardInfo userLeaderBoard;
    private List<LeaderBoardInfo> leaderBoardInfoList;
}
