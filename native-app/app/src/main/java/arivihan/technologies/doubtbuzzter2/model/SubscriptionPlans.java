package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SubscriptionPlans implements Serializable {
    private String planId;
    private String name;
    private boolean bestValue;
    private String perMonthPrice;
    private String savingBadgeText;
    private String costPrice;
    private String finalPrice;
    private boolean selected;
    private DurationUnit durationUnit;
    private String duration;
    private String studentsRegistered;
    private String validForNextTime;
    private CouponDiscountType extraDiscountType;
    private String extraDiscountValue;
    private boolean discountApplicable;
    private String requestId;
    private String coupon;
    private String coinValue;
}
