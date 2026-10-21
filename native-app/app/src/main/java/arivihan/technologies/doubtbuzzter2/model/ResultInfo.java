package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class ResultInfo implements Serializable {
    String infoStatus;
    String rejectMessage;
    String verifiedBy;
    Date verifiedOn;
    float percentDiscount;
    String couponName;
    String couponText;
    String expiredCouponText;
    boolean isChecked;
    String subscriptionId;
    String subscriptionName;
    String principalAmount; // Principal amount
    String offerAmount; // Actual Offer amount
    String finalAmount; // Offer amount with discount
    String imageUrl;
    long expiryTime;

    @Override
    public String toString() {
        return "Resultinfo{" +
                "infoStatus='" + infoStatus + '\'' +
                ", rejectMessage='" + rejectMessage + '\'' +
                ", verifiedBy='" + verifiedBy + '\'' +
                ", verifiedOn=" + verifiedOn +
                ", percentDiscount=" + percentDiscount +
                ", couponName='" + couponName + '\'' +
                ", isChecked=" + isChecked +
                ", offerAmount='" + offerAmount + '\'' +
                ", expiredCouponText='" + expiredCouponText + '\'' +
                ", subscriptionId='" + subscriptionId + '\'' +
                ", subscriptionName='" + subscriptionName + '\'' +
                ", principalAmount='" + principalAmount + '\'' +
                ", finalAmount='" + finalAmount + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
