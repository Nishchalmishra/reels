package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SubscriptionRenewalReward implements Serializable {
    String userId;
    String couponCode;
    String couponCodeText;
    double discountedPrice;
    int discountPercentage;
    long createdAt;
    long validFrom;
    long validTo;
    double netAfterRenewal;
}
