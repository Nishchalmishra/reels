package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CampaignSubscriptionScreenContactDto implements Serializable {
    private String phoneNumber;
    private String floatingActionButtonText;
    private String posterImage;
    private String audioUrl;
    private String backText;
    private String joinNowText;
    private String contactSection;
    private String callBackText;
}
