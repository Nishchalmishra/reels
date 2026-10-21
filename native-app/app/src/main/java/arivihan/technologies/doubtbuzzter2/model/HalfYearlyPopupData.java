package arivihan.technologies.doubtbuzzter2.model;/*
 * File: HalfYearlyPopupData Copyright 06/01/25, 7:59 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 06/01/25, 7:59 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class HalfYearlyPopupData implements Serializable {
    private String title;
    private String messageOne;
    private String messageTwo;
    private String messageThree;
    private String messageFour;
    private String btnText;
    private String thumbnail;
    private String videoUrl;
    private String className;
    private String activityParams;
}
