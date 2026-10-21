package arivihan.technologies.doubtbuzzter2.model.competitive.banner;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.enums.competitive.BannerType;
import arivihan.technologies.doubtbuzzter2.model.NavigationConfig;
import arivihan.technologies.doubtbuzzter2.model.remote_config.ActivityNavigationConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDto implements Serializable {
    private BannerType bannerType;
    private String type;  // static/dynamic
    private String backgroundImage;
    private String title;
    private String subTitle;
    private Integer totalTopicCount;
    private Integer remainingCount;
    private String imageUrl;
    private String imageType;
    private Boolean isSubscribed;
    private ActivityNavigationConfig activityNavigationConfig;
}
