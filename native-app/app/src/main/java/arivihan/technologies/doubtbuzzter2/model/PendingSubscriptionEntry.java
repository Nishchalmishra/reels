package arivihan.technologies.doubtbuzzter2.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingSubscriptionEntry {
    String name;
    String planId;
    String status;
    String language;
}
