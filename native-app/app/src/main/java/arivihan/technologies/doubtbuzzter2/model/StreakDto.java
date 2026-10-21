package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: StreakDto Copyright 07/06/21, 11:49 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 07/06/21, 11:49 PM
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
public class StreakDto {
    private String message;
    private String levelName;
    private int timing;
    private int score;
    private int pointToEarn;
    private boolean locked;
    private boolean solved;
    private int startedTime;
}
