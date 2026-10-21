package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubscriptionRenewalReminder implements Serializable {
    String userId;
    String classIdCourseId;
    String classId;
    String courseId;
    String subscriptionName;
    String subscriptionId;
    String planLevel;
    int planLevelPriority;
    double amount;
    int count;
    String couponCode;
    String reminderTextCode;
    String reminderText;
    String reminderTitle;
    double discountedPrice;
    int discountPercentage;
    String poster;
}
