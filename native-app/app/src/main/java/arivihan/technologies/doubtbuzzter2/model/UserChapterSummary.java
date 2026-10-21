package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserChapterSummary {

     String title;
     ArrayList<Tabs> tabs;
     String type;
     ArrayList<ChapterSummaryDetail> list;
}
