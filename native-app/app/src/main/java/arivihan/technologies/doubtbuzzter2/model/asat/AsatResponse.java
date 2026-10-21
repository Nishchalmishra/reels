package arivihan.technologies.doubtbuzzter2.model.asat;

/*
 * File: ApiResult Copyright 20/1/22 8:37 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 20/1/22 8:37 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsatResponse<T> implements Serializable {
    private String message;
    private int code;
    private T data;
}
