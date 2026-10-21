package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: PreviousVideoContent Copyright 10/4/21 1:01 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 10/4/21 1:01 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PreviousVideoContent {
    private String VideoUrl;
    private long prevRunningPosition;
    private long currentVideoCompletionStartPos;
    private long currentVideoCompletionEndpos;
}
