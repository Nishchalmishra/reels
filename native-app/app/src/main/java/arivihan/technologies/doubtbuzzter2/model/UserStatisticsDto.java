package arivihan.technologies.doubtbuzzter2.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class UserStatisticsDto {

    private int score;
    private String currentLevelId;
    private String nextUnlockTime;
    private List<GameLevelDto> gameLevelDtoList;
    private Long startedOn;
    private String currentGameId;
}
