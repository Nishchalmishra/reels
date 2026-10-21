package arivihan.technologies.doubtbuzzter2.model;/*
 * File: TimeBarThumbData Copyright 25/09/24, 5:10 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 25/09/24, 5:10 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeBarThumbData {
    private String questionId;
    private long position;
    private boolean completed;
    private boolean hidden;
    private int elevation;
    private boolean correct;
    private boolean active;
}
