package arivihan.technologies.doubtbuzzter2.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SubscriptionModel implements Serializable {

    private String title;
    private String videoUrl;
    private String thumbnailUrl;
    private String subscriptionType;
    private ArrayList<SubscriptionOptions> subscriptionOptions;
    private ArrayList<SubscriptionPlans> plans;
    private SubscriptionOverview overview;
    private String planLevel;
    private String selectedPlanId;
    private boolean comboSelected;
    private boolean boardSelected;
    private String renewCouponCodeText;
    private String renewCouponCode;
    private int discountPercent;
    private double discountAmount;
    private SubscriptionRenewalReward subscriptionRenewalReward;
    private ArrayList<PlanLevel> planLevels;
    private boolean toShowSubscriptionPlanList;

    //
    private String bannerImageUrl;
    private String badgeImageUrl;
    private String featureTitle;
    private ArrayList<String> features;
    private PriceBreakdownModel pricingBreakdown;

    @SerializedName("planMetadata")
    private SubscriptionPlanMetaData planMetaData;
}
