package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameLevelModel {

    private String levelId;
    private String ToWin;
    private String UnLockTime;
    private String levelName;
}
