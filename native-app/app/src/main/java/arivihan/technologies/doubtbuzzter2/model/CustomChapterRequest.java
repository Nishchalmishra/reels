package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: CustomChapterRequest Copyright 13/05/23, 7:22 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 13/05/23, 7:22 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomChapterRequest {
    private String topic;
    private String description;
    private String selectedSubject;
    private String selectedChapter;
    private String selectedExam;
    private String selectedLevel;
}
