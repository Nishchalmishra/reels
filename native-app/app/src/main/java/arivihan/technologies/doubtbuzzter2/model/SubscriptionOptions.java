package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriptionOptions implements Serializable {
    private String name;
    private String costPrice;
    private String finalPrice;
    private boolean selected;
    private String duration;
}
