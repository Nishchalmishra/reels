package arivihan.technologies.doubtbuzzter2.model.user.batch.subscription;

import java.io.Serializable;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInfo implements Serializable {
    private String subscriptionType;
    private String subscriptionPlanId;
    private String subscriptionCompletedVideoUrl;
    private boolean isActive;
    private Set<PlanFeatureRules> featureRules;
}
