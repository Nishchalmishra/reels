package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: DoubtResponseDto Copyright 24/02/21, 12:14 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 24/02/21, 12:14 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubtResponseDto implements Serializable {
    private String contentUrl;
    private int length;
    private boolean found;
    private LectureContentType lectureContentType;
    private long position;
    private long prevVideoPosition;
    private String prevMicroLectureId;
    private boolean conversation;
    private String messageToShow;
    private boolean sameMicrolecture;
    private String intentId;
    private String textSolution;
    private String imageSolution;
    private boolean textSol;
    private boolean disambiguation;

    private boolean ttvSolution;
    private boolean notInAskedLanguage;
    private long timeTaken;
    private List<MLResponse> allResults;
    private List<String> googleResults;

    @Override
    public String toString() {
        return "DoubtResponseDto{" +
                "contentUrl='" + contentUrl + '\'' +
                ", length=" + length +
                ", found=" + found +
                ", position=" + position +
                ", prevVideoPosition=" + prevVideoPosition +
                ", prevMicroLectureId='" + prevMicroLectureId + '\'' +
                ", conversation=" + conversation +
                ", messageToShow='" + messageToShow + '\'' +
                ", sameMicrolecture=" + sameMicrolecture +
                ", intentId='" + intentId + '\'' +
                ", textSolution='" + textSolution + '\'' +
                ", imageSolution='" + imageSolution + '\'' +
                ", textSol=" + textSol +
                ", disambiguation=" + disambiguation +
                ", timeTaken=" + timeTaken +
                '}';
    }
}
