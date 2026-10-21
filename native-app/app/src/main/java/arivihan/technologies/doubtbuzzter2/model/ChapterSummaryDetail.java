package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChapterSummaryDetail {
    String title;
    String type;
    String color;
    ArrayList<SectionModel> dataList;
}
