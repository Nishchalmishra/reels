package arivihan.technologies.doubtbuzzter2.model.competitive.subject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import arivihan.technologies.doubtbuzzter2.model.NameAndIdDto;
import arivihan.technologies.doubtbuzzter2.model.competitive.banner.BannerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicWiseChapterListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<CompetitiveChapterDto> chapters;
    private BannerDto banner;
    private List<NameAndIdDto> subjects;
    private List<String> priorityFilterOptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterTrend implements Serializable {
        private static final long serialVersionUID = 1L;

        private String chapterTrendText;
        private String bgColor;
        private String textColor;
    }
}

