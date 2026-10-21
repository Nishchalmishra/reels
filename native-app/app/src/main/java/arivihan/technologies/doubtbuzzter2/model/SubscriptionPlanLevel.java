package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: SubscriptionPlanLevel Copyright 22/3/22 2:06 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 22/3/22 2:06 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionPlanLevel implements Serializable {
    private String subscriptionPlan;
    private String value;
    private List<NameAndIdDto> subTypeList;
}
