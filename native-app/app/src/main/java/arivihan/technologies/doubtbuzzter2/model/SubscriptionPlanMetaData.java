package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriptionPlanMetaData implements Serializable {
    private String title;

    private CampaignSubscriptionScreenContactDto campaignSubscriptionScreenContactDto;

    private BatchPdfDetails batchPdfDetails;

    private String planValidateText;

    private String planBenefitsImageUrl;
    private String thumbnailUrl;
    private String videoUrl;
    private String subscriptionCompletedVideoUrl;
    private String shareQrCodeMessage;
    private String qrCodeSectionTitle;
    private String qrCodeSectionSubTitle;

    @Data
    public class BatchPdfDetails implements Serializable {

        private String title;
        private String subtitle;
        private String pdfUrl;
        private String buttonText;
    }
}
