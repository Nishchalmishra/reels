package arivihan.technologies.doubtbuzzter2.model;/*
 * File: ToDoRecyclerItem Copyright 16/07/23, 12:12 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 16/07/23, 12:12 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ToDoRecyclerItem {
    private ToDoContentType type;
    private boolean watched;
    private String title;
    private String subTitle;
    private String id;
}
