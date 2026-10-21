package arivihan.technologies.doubtbuzzter2.model.crash_course;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import arivihan.technologies.doubtbuzzter2.model.todo.TodayClassGuideVideoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayClassResponse {
    private String layoutTitle;
    private TodayTaskListType fragmentType;
    private String layoutDescription;
    private boolean showTimer;
    private TodayClassGuideVideoDto guideVideo;
    private ActivityNavigationConfig seeAllConfig;
    private List<TodayClassItemResponse> classes;
}
