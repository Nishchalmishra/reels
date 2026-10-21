package arivihan.technologies.doubtbuzzter2.listener;/*
 * File: OnQuestionListener Copyright 09/12/23, 2:46 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 09/12/23, 2:46 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.model.BoardLectureExpandedModel;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;

public interface OnQuestionListener {
    void lastExpanded(int position);
    void questionClicked(MicroLectureContentDto microLectureContentDto, int position, ArrayList<BoardLectureExpandedModel> list);

    void notesClicked(String contentUrl);

    void oneExpandClicked(int x, int y);
}
