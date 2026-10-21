package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionModel {

    private String id;
    private String subscriptionPlanId;
    private String Title;
    private String OrderNo;
    private String Date;
    private String price;
    private boolean onGoing;
    private String paymentStatus;
    private String invoiceDownloadUrl;
    private String subscriptionPlanLevel;
    private String subscriptionPlanLevelValue;
    private SubscriptionStatus subscriptionStatus;
    private int duration;

}
