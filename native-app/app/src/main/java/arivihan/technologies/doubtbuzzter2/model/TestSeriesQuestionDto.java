package arivihan.technologies.doubtbuzzter2.model;/*
 * File: TestSeriesQuestionDto Copyright 1/6/22, 3:09 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 1/6/22, 3:09 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSeriesQuestionDto implements Serializable, Parcelable {
    private String title;
    private int fragNumber;
    private int score;
    private int userScore;
    private double precisionUserScore;
    private String subtext;
    private InfoType infoType;
    private String userAnswer;
    private boolean correct;
    private long timeToAttempt;
    private Long timeTaken;
    private int negativeScore;
    private String questionId;
    private String questionText;
    private String questionImage;
    private String explanationImage;
    public String answer;
    private String explanation;
    private String questionType;
    private String difficultyLevel;
    public List<String> options;
    public List<String> answers;
    private List<OptionDto> optionDtos;
    private String subject;
    private String chapter;
    private String unit;
    private boolean marked;
    private AnswerState answerState;
    private QuestionType questionTypeEnum;
    private List<String> imageOptions;
    private HashMap<String,Integer> limit;
    private String optionalTag;
    private String optionType; // Either TEXT / IMAGE / NULL
    private String year; // For Year Tag - e.g 2025

    private String videoUrl;
    private long videoStartingTime;
    private long videoEndingTime;
    private boolean savType;
    private String comprehensiveCaseStudyText;
    private String comprehensiveCaseStudyImage;
    private String comprehensiveQuesNo;
    private String caseStudyGroup;
    private boolean hideViewSolution;
    private boolean typedAnswer;
    public List<Uri> uriList;
    private String wordBufferLimit;
    private String wordLimit;
    public boolean wordLimitApplicable;

    protected TestSeriesQuestionDto(Parcel in) {
        title = in.readString();
        score = in.readInt();
        subtext = in.readString();
        userAnswer = in.readString();
        correct = in.readByte() != 0;
        timeToAttempt = in.readLong();
        timeTaken = in.readLong();
        negativeScore = in.readInt();
        questionId = in.readString();
        questionText = in.readString();
        questionImage = in.readString();
        answer = in.readString();
        explanation = in.readString();
        questionType = in.readString();
        difficultyLevel = in.readString();
        options = in.createStringArrayList();
        answers = in.createStringArrayList();
        optionDtos = in.createTypedArrayList(OptionDto.CREATOR);
        subject = in.readString();
        chapter = in.readString();
        unit = in.readString();
        marked = in.readByte() != 0;
        imageOptions = in.createStringArrayList();
        optionalTag = in.readString();
        optionType = in.readString();
        explanationImage = in.readString();
        videoUrl = in.readString();
        videoStartingTime = in.readLong();
        videoEndingTime = in.readLong();
        savType = in.readByte() != 0;
        comprehensiveCaseStudyText = in.readString();
        comprehensiveCaseStudyImage = in.readString();
        comprehensiveQuesNo = in.readString();
        caseStudyGroup=in.readString();
        hideViewSolution = in.readByte() != 0;
        typedAnswer = in.readByte() != 0;
        year = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(score);
        dest.writeString(subtext);
        dest.writeString(userAnswer);
        dest.writeByte((byte) (correct ? 1 : 0));
        dest.writeLong(timeToAttempt);
        dest.writeLong(timeTaken);
        dest.writeInt(negativeScore);
        dest.writeString(questionId);
        dest.writeString(questionText);
        dest.writeString(questionImage);
        dest.writeString(answer);
        dest.writeString(explanation);
        dest.writeString(questionType);
        dest.writeString(difficultyLevel);
        dest.writeStringList(options);
        dest.writeStringList(answers);
        dest.writeTypedList(optionDtos);
        dest.writeString(subject);
        dest.writeString(chapter);
        dest.writeString(unit);
        dest.writeByte((byte) (marked ? 1 : 0));
        dest.writeStringList(imageOptions);
        dest.writeString(optionalTag);
        dest.writeString(optionType);
        dest.writeString(explanationImage);
        dest.writeString(videoUrl);
        dest.writeLong(videoStartingTime);
        dest.writeLong(videoEndingTime);
        dest.writeByte((byte) (savType ? 1 : 0));
        dest.writeString(comprehensiveCaseStudyText);
        dest.writeString(comprehensiveCaseStudyImage);
        dest.writeString(comprehensiveQuesNo);
        dest.writeString(caseStudyGroup);
        dest.writeByte((byte) (hideViewSolution ? 1 : 0));
        dest.writeByte((byte) (typedAnswer ? 1 : 0));
        dest.writeString(year);
    }

    public String getQuestionText() {
        if (StringUtils.isEmpty(questionText)) {
            return "";
        }
        return questionText;
    }

    public AnswerState getAnswerState() {
        return Objects.requireNonNullElse(answerState, AnswerState.UNANSWERED);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TestSeriesQuestionDto> CREATOR = new Creator<TestSeriesQuestionDto>() {
        @Override
        public TestSeriesQuestionDto createFromParcel(Parcel in) {
            return new TestSeriesQuestionDto(in);
        }

        @Override
        public TestSeriesQuestionDto[] newArray(int size) {
            return new TestSeriesQuestionDto[size];
        }
    };

    public QuestionType getQuestionTypeEnum() {
        return QuestionType.valueOf(questionType);
    }

    @Override
    public String toString() {
        return "TestSeriesQuestionDto{" +
                "questionId='" + questionId + '\'' +
                ", questionText='" + questionText + '\'' +
                ", options=" + options +
                ", optionDtos=" + optionDtos +
                ", questionTypeEnum=" + getQuestionTypeEnum() +
                ", imageOptions=" + imageOptions +
                '}';
    }
}