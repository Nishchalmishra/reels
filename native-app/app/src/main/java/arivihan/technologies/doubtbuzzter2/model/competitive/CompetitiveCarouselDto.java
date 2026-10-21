package arivihan.technologies.doubtbuzzter2.model.competitive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitiveCarouselDto {
    private String url;
    private String className;
    private String activityParams;
    private boolean isForSubscribedUser;

}
