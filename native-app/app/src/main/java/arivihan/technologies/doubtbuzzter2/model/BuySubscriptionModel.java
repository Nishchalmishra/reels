package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class BuySubscriptionModel implements Serializable {
    SubscriptionPlans subscriptionPlan;
    boolean showSpecialOffer;
    SubscriptionPlans specialOffer;
    PriceBreakdownModel pricingBreakdown;
    SubscriptionOverview overview;
}
