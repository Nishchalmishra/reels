package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: MicroLectureContentDto Copyright 14/2/21 12:31 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 12:31 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.constants.QuestionLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroLectureContentDto implements Serializable {
    private String chapterName;
    private String chapterId;
    private String id;
    private String title;
    public String langTitle;
    private String header_title;
    private String QuestionImage;
    private String questionId;
    private QuestionLevel questionLevel;
    private String code;
    private boolean header;
    private String microlectureThumbnail;
    private LectureContentType lectureContentType;
    private int defaultStartTime;
    private int videoCompletionStartPos;
    private int videoCompletionEndPos;
    private long completionValue =0;
    private boolean completed;
    private String contentUrl;
    private String videoUrl;
    private String question;
    private List<String> options;
    private List<String> answers;
    private String answer;
    private String answerEx;
    private int progress;
    private long resumePosition;
    private String answerVideo;
    private String answerImage;
    private String answerSolImage;
    private boolean encrypted;
    private boolean converted;
    private long videoCompletionPausePos;
    private boolean personalised;
    private List<LanguageAndVideoUrlDto> languageAndVideoUrlDtos;
    private String marksObtained;
    private String marks;
    private String userSolution;
    public String wordLimit;
    public boolean wordLimitApplicable;
    public List<VideoQualityDto> videoQualityDtos;
    private long likeCount;
    private Boolean liked;
    private boolean isLikeDislikeVisible; // flag for local

    public String getWordLimit() {
        if (StringUtils.isEmpty(wordLimit)) {
            return "350";
        }
        return wordLimit;
    }

    public String optionType; // Either TEXT / IMAGE / NULL

    public String getMarks() {

        if (StringUtils.isEmpty(marks) && lectureContentType.getType() != null &&
                lectureContentType.getType().equalsIgnoreCase(LectureContentType.LONG_ANSWERS.getType())) {
            return "5";
        }
        if (StringUtils.isEmpty(marks) && lectureContentType.getType() != null &&
                lectureContentType.getType().equalsIgnoreCase(LectureContentType.SHORT_ANSWERS.getType())) {
            return "3";
        }
        return marks;
    }



}
