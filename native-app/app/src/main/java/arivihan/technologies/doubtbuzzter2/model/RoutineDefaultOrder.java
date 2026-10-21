package arivihan.technologies.doubtbuzzter2.model;/*
 * File: RoutineDefaultOrder Copyright 23/08/23, 12:57 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/08/23, 12:57 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutineDefaultOrder {
    String message;
    int code;
    Map<String, List<UserSyllabusOrder>> data;
}
