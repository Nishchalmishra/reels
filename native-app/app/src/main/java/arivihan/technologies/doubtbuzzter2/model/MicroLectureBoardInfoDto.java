package arivihan.technologies.doubtbuzzter2.model;/*
 * File: MicroLectureBoardInfoDto Copyright 09/12/23, 1:44 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 09/12/23, 1:44 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroLectureBoardInfoDto implements Serializable {
    private String id;
    private boolean personalized;
    private String message;
    private String title;
    private String code;
    private boolean locked;
    private int progress;
    private Date createdAt;
    private Boolean isNotWatched;
    private String notesUrl;
    private MicroLectureContentDto videoContent;
    private MicrolectureBoardContentInfoDto mcqQuestion;
    private MicrolectureBoardContentInfoDto oneWordQuestion;
    private MicrolectureBoardContentInfoDto numericalQuestion;
    private MicrolectureBoardContentInfoDto longAnswers;
    private MicrolectureBoardContentInfoDto shortAnswers;
    private MicrolectureBoardContentInfoDto veryShortAnswers;
    private MicrolectureBoardContentInfoDto trueOrFalse;
    private MicrolectureBoardContentInfoDto matchTheColumn;
    private MicrolectureBoardContentInfoDto fillInTheBlanks;
}
