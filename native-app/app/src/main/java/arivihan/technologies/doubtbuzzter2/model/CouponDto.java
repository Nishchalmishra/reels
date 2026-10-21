package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: CouponDto Copyright 22/3/22 4:54 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 22/3/22 4:54 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponDto implements Serializable {
    private String id;
    private String code;
    private String description;
    private Long validFrom;
    private Long validTo;
    private Boolean active;
    private boolean expired;
    private int discountApplied;

    public boolean isExpired() {
        if (active == null || validTo == null) return false;
        return !active && System.currentTimeMillis() > validTo;
    }

}
