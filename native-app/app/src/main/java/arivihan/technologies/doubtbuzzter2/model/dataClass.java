package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class dataClass {

    private List<SubjectProgressDto> subjectProgressDtoList;
    private List<ChapterProgressDto> chapterProgressDtoList;
    private LectureAnalytics lectureAnalytics;
    private int testsAttempted;
    private int correctAnswerPercentage;
    private int noOfHard;
    private int noOfMedium;
    private int noOfEasy;
    private int totalHard;
    private int totalMedium;
    private int totalEasy;
    private int totalQuestion;

}
