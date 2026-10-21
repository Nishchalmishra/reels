package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: SubscriptionPreModel Copyright 23/3/22 10:54 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/3/22 10:54 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionPostModel implements Serializable {
    private String subscriptionId;
    private String invoiceId;
    private String orderId;
    private String paymentStatus;
    private String transactionId;
    private String requestId;
    private boolean internalMetrics;
    private String secretKey;
}
