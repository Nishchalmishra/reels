package arivihan.technologies.doubtbuzzter2.model.firebase.rdb;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
public class NeetWeakTopicRdbModel implements Serializable {
    private String userId;
    private String status;
    private String title;
    private String subtitle;
    private String popUpTitle;
    private String popUpSubTitle;
    private Long createdAt;

    private String homeScreenTitle;
    private String homeScreenSubTitle;
    private String homeScreenBtnText;
}
