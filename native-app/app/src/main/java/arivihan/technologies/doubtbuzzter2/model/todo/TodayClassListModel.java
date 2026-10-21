package arivihan.technologies.doubtbuzzter2.model.todo;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.enums.TodayTaskListType;
import arivihan.technologies.doubtbuzzter2.model.TodayClassDto;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TodayClassListModel implements Serializable {
    private TodayTaskListType fragmentType;
    private String layoutTitle;
    private String layoutDescription;
    private TodayClassGuideVideoDto guideVideo;
    private boolean showTimer;
    private ActivityNavigationConfig seeAllConfig;
    List<TodayClassDto> todayClassDto;
}
