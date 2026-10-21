package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockTestChapterListModel {

    public String TestId;
    public String Chapter;
    public String Questions;
    public long Time;
    public Boolean Attempted;
    public long TotalScore;
    public long YourScore;
    private List<NameAndScoreDto> nameAndScoreDtoList;

}
