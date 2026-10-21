package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class TestAnalyticsData implements Serializable {


    private String subject;
    private List<TestChapterAnalysisDetails> testChapterAnalysisDetails;



}
