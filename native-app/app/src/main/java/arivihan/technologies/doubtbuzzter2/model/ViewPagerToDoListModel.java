package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: ViewPagerToDoList Copyright 11/07/23, 9:14 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 11/07/23, 9:14 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewPagerToDoListModel {
    private String date;
    private String name;
    private ViewType viewType;
    private List<ToDoRecyclerItem> toDoRecyclerItemList;
}
