package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: InvoiceDto Copyright 23/3/22 3:50 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/3/22 3:50 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private String id;
    private Double amountPaid;
    private String subscriptionPlanId;
    private Long validFrom;
    private Long validTo;
    private String title;
    private String expiryDate;
    private String purchasedDate;
    private String lastTrasactionId;
    private long lastPayement;
    private Long currentValidFrom;
    private Long currentValidTo;
    private Boolean active;
    private Long purchasedOn;
    private Double totalAmount;
    private Double emiAmount;
    private Integer numberOfEmis;
    private Integer totalEmisPaid;
    private SubscriptionStatus status;
    private PaymentStatus paymentStatus;
    private Long createdAt;
    private Long modifiedAt;
    private String invoiceId;
    private CouponDto coupon;
    private Integer arivihanPointsUsed;
    private String transactionId;
    private Double discountApplied;
}
