package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: CompetitionHistory Copyright 26/5/22 9:40 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 26/5/22 9:40 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetitionHistory implements Serializable {
    private String gameId;
    private String gameName;
    private int score;
    private String timeTaken;
    private List<List<String>> instructions;
    private GameSyllabus gameSyllabus;
    private String playedOn;
}

