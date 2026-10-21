package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class PaymentDetails {
    private String userId;
    private Date date;
    private long amount;
    private String orderId;
    private String type;
    private int pointsToAdd;
    private int currentPoints;
    private String paymentMode;
    private String qrCodeId;
}
