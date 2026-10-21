package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.Data;

@Data
public class UserScoreComparison {
    ArrayList<Tabs> tabs;
    ArrayList<String> xaxisLabel;
    ArrayList<String> xasisRange;
    ArrayList<ScoreComparisonValue> series;

}
