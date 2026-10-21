package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: MLResponse Copyright 4/10/22 11:05 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 4/10/22 11:05 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MLResponse implements Serializable {
    private String intentid;
    private String imageSolution;
    private String match_type;
    private Double score;
    private Double time;
    private boolean textSol;
    private String textSolution;
    private String textToDisplay;
    private String contentUrl;
    private int length;
    private long position;
    private boolean ttvSolution;
    private boolean notInAskedLanguage;
}
