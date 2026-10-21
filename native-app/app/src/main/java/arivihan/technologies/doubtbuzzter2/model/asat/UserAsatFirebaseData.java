package arivihan.technologies.doubtbuzzter2.model.asat;/*
 * File: UserAsatFirebaseData Copyright 19/06/25, 2:17 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 19/06/25, 2:17 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class UserAsatFirebaseData implements Serializable {
    private String title;
    private String subTitle;
    private String language;
}
