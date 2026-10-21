package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * File: StreakLevelDto Copyright 13/6/21 12:14 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 13/6/21 12:14 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakLevelDto implements Comparable<StreakLevelDto>, Serializable {
    private String levelName;
    private String message;
    private int pointToEarn;
    private int score;
    private int timeToSolve;
    private int timing;
    private boolean locked = true;
    private boolean showTimer = false;

    @Override
    public int compareTo(StreakLevelDto o) {
        return levelName.compareToIgnoreCase(o.getLevelName());
    }
}
