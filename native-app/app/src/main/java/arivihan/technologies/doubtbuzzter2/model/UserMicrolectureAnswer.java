package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: UserMicrolectureAnswer Copyright 23/1/22 6:22 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/1/22 6:22 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import arivihan.technologies.doubtbuzzter2.constants.QuestionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMicrolectureAnswer implements Serializable {
    String microlectureId;
    String chapterId;
    String questionId;
    String userAnswer;
    String question;
    String obtainedMarks;
    String totalMarks;
    String explanation;
    QuestionType questionType;
    String title;
    QuestionLevel questionLevel;
    Long timeTaken;
    boolean correct;
    String wrongAnswer;
    String correctAnswer;
    String options;
}
