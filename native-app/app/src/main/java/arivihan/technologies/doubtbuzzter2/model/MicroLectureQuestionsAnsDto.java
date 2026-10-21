package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;



/*
 * File: MicroLectureQuestionsAns Copyright 15/2/21 7:54 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 15/2/21 7:54 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroLectureQuestionsAnsDto implements Serializable {
    private String id;
    private String question;
    private Integer startPosition;
    private Integer endPosition;
    private Integer correctAnswerSeekTo;
    private Integer wrongAnswerSeekTo;
    private String correctAnswerIntentId;
    private String wrongAnswerIntentId;
    private Integer pos;
    @Override
    public String toString() {
        return "MicroLectureQuestionsAnsDto{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", correctAnswerSeekTo=" + correctAnswerSeekTo +
                ", wrongAnswerSeekTo=" + wrongAnswerSeekTo +
                ", correctAnswerIntentId='" + correctAnswerIntentId + '\'' +
                ", wrongAnswerIntentId='" + wrongAnswerIntentId + '\'' +
                ", pos=" + pos +
                '}';
    }
}
