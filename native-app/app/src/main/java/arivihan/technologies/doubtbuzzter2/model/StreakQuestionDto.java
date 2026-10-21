package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: StreakQuestionDto Copyright 13/6/21 9:02 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 13/6/21 9:02 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakQuestionDto implements Serializable {
    private String id;
    private String question;
    private List<String> options;
    private String solText;
    private String solImage;
    private boolean intType;
    private int intAns;
    private String optionAnswer;
}
