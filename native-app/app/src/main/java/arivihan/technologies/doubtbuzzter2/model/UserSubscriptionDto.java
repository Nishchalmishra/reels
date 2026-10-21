package arivihan.technologies.doubtbuzzter2.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import lombok.*;


@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSubscriptionDto implements Serializable {
    private String id;
    private String userId;
    private String subscriptionPlanId;
    private Long validFrom;
    private Long validTo;
    private String title;
    private String expiryDate;
    private String purchasedDate;
    private String lastTrasactionId;
    private long lastPayement;
    private Long currentValidFrom;
    private Long currentValidTo;
    private Boolean active;
    private Long purchasedOn;
    private Double totalAmount;
    private Double amountPaid;
    private Double emiAmount;
    private Integer numberOfEmis;
    private Integer totalEmisPaid;
    private SubscriptionStatus status;
    private PaymentStatus paymentStatus;
    private Long createdAt;
    private Long modifiedAt;
    private String invoiceId;
    private CouponDto coupon;
    private Integer arivihanPointsUsed;
    private Double discountApplied;
    private String transactionId;
    private String orderId;
    private String invoiceDownloadUrl;
    //private SubscriptionPlanDescriptionDto planDescription;
    private String subscriptionPlanLevel;
    private String subscriptionPlanLevelValue;
    private SubscriptionDuration duration;
}
