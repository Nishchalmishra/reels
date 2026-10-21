package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.Data;

@Data
public class SubscriptionResult {
    List<SubscriptionBenefitsModel> list;
    String response;
}
