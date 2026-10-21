package arivihan.technologies.doubtbuzzter2.model;/*
 * File: StudentProgressDto Copyright 09/08/24, 5:26 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 09/08/24, 5:26 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentProgressDto implements Serializable {
    private String SubjectName;
    private int progress;
}
