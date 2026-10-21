package arivihan.technologies.doubtbuzzter2.model.competitive.subject;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.topic.PyqsInformationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitiveChapterDto implements Serializable {
    private String chapterId;
    private String chapterCode;
    private String chapterIconUrl;
    private String title;
    private Integer noOfTopics;
    private String chapterPriority;
    private TopicWiseChapterListResponse.ChapterTrend chapterTrend;
}
