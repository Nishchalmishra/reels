package arivihan.technologies.doubtbuzzter2.listener;

/*
 * File: ChapterDetailActivityListener Copyright 14/2/21 12:41 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 12:41 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;

public interface ChapterDetailActivityListener {
    void goToActivity(MicroLectureContentDto microLectureContentDto, int position);
}
