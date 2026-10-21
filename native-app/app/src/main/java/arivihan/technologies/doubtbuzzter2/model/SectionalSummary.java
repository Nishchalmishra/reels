package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SectionalSummary {
    ArrayList<Tabs> tabs;
    int userScore;
    double precisionUserScore;
    int totalScore;
    ArrayList<SectionModel> sectionList;
    ArrayList<MockAverageSpeed> averageSpeeds;
    ArrayList<AttemptAnalysisData> attemptList;
}
