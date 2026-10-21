package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDescriptionModel implements Serializable {

    private String id;
    private String description;
    private String courseId;
    private String classId;
    private String subjectId;
    private String chapterId;
    private String testSeriesId;
    private String subscriptionPlanLevel;
    private CouponDto coupon;
    private long price;
    private long createdAt;
    private long modifiedAt;
    private String subtypeName;
    private Duration duration;
    private PlanDescription planDescription;
    private int arivihanPointsUsed;
    //not used now
    private int internalDiscount;
    private int couponDiscount;
    private InvoiceDto invoice;



    @Override
    public String toString() {
        return "SubscriptionDescriptionModel{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", courseId='" + courseId + '\'' +
                ", classId='" + classId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", chapterId='" + chapterId + '\'' +
                '}';
    }
}
