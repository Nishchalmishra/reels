package arivihan.technologies.doubtbuzzter2.model.user.batch.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanFeatureRules {
    private String featureKey;
    private String accessType;
    private String visibility;
    private String limitType;
    private int limitValue;
}