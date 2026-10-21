package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: UserStreakData Copyright 13/6/21 12:06 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 13/6/21 12:06 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakData implements Serializable {
    public Integer currentLevel = 1;
    public Integer currentStreakPoint = 0;
    public Long lastVisited = 0L;
    private Long oldPoint = 0L;
    private String currentQuestionLevelId;
    public long nextUnlockTime = 0L;
    //key -> version +  level name +  id, time in millisecond
    public Map<String, Long> levelStartTime = new HashMap<>();
    //key -> version +  level name +  id
    public Map<String, Boolean> levelVisitedWonMap = new HashMap<>();
    public List<String> playedStreakVersion = new ArrayList<>();
}
