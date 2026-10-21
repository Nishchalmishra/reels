package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: RankListDto Copyright 1/2/23 6:57 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 1/2/23 6:57 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.util.List;

import lombok.Data;

@Data
public class RankListDto {
    private int averageScore;
    private String message;
    private List<LeaderboardMockTestModel> rankDtoList;
}
