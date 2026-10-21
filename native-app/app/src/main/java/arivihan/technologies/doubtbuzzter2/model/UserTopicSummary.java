package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTopicSummary {
    String title;
    ArrayList<Tabs> tabs;
    String type;
    ArrayList<ChapterSummaryDetail> list;
    private ArrayList<ChapterSummaryDetail> topicWiseList;
}
