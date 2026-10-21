package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: MicroLectureQuestionAnsResponseDto Copyright 4/11/21, 5:57 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 4/11/21, 5:57 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MicroLectureQuestionAnsResponseDto implements Serializable {
    private String id;
    private boolean found;
    private Integer seekTo;
    private String questionText;
    private String questionImage;
    private Integer trackerCompletionStartPos;
    private Integer trackerCompletionEndPos;
    private Integer fallbackPos;
    private List<ConfirmOption> confirmOptionList;
    private boolean correctMatched;

    @NonNull
    @Override
    public String toString() {
        return "MicroLectureQuestionAnsResponseDto{" +
                "id='" + id + '\'' +
                ", found=" + found +
                ", seekTo=" + seekTo +
                ", trackerCompletionStartPos=" + trackerCompletionStartPos +
                ", trackerCompletionEndPos=" + trackerCompletionEndPos +
                ", fallbackPos=" + fallbackPos +
                ", correctMatched=" + correctMatched +
                ",confirmOptionList="+confirmOptionList+
                '}';
    }
}
