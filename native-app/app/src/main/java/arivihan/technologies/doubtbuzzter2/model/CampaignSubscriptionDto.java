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
public class CampaignSubscriptionDto implements Serializable {
    String thumbnailUrl;
    String videoUrl;
    String screenText;
    ArrayList<String> featuresList;
    String subscriptionBatchInfoPdf;
    private CampaignSubscriptionScreenContactDto campaignSubscriptionScreenContactDto;
}
