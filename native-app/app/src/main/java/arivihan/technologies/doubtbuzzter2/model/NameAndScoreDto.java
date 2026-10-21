package arivihan.technologies.doubtbuzzter2.model;/*
 * File: NameAndScoreDto Copyright 28/05/23, 11:16 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 28/05/23, 11:16 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NameAndScoreDto {

    private String subjectName;
    private int score;
}
