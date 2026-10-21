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
public class SubscriptionPopUpModel implements Serializable {
    private String requestId;
    private String title;
    private String upiId;
    private Long finalAmount;
    private String planId;
    private String planName;
    private Long price;
    private String mentorName;
    private String mentorId;
    private long expiryDuration;
    private String userId;
    private String paymentReceivedBy;
    private String paymentReceivedByMetricsUserId;
    private String paymentReceivedByUserName;
    private String paymentReceivedByPhone;
    private String paymentRaisedFrom;
    private String couponCode;
    private long createdAt;
    private long updatedAt;
}
