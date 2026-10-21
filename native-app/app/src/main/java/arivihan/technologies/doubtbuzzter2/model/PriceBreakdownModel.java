package arivihan.technologies.doubtbuzzter2.model;

import arivihan.technologies.doubtbuzzter2.model.subscription.RewardCoins;

import lombok.Data;

@Data
public class PriceBreakdownModel {
    String total;
    String couponCodeValue;
    boolean couponCodeValid;
    String subscriptionPrice;
    String couponCode;
    String couponCodeDeduction;
    private SubscriptionRenewalReward renewalReward;
    private CouponDiscountType couponDiscountType;
    private RewardCoins rewardCoins;
    private PaymentSummary paymentSummary;

    @Data
    public static class PaymentSummary {
        private int basePrice;
        private int totalDiscountAmount;
        private int totalDiscountPercentage;
        private int totalPayableAmountWithoutRewardCoins;
        private int rewardCoins;
        private int totalPayableAmount;
    }
}
