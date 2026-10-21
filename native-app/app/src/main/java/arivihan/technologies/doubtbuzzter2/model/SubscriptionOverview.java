package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriptionOverview implements Serializable {
    private String costPrice;
    private String finalPrice;
    private String duration;
    private DurationUnit durationUnit;
    private String durationLabel;
    private String planId;
    private String name;
}
