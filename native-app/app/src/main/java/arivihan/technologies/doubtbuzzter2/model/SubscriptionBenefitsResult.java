package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionBenefitsResult implements Serializable {
    private String code;
    private String message;
    private boolean success;
    private String congratulationHeader;
    private String congratulationSubHeader;
    private List<SubscriptionBenefitsModel> data;
}
