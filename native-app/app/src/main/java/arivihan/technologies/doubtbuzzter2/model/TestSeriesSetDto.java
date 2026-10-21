package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: TestSeriesSetDto Copyright 7/1/22 7:36 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 7/1/22 7:36 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSeriesSetDto implements Parcelable, Serializable {
    private String id;
    private String syllabusInfo;
    private String testSeriesId;
    private boolean expanded;
    private String testSeriesName;
    private long totalScore;
    private long totalTimeToAttempt;
    private long userTimeTaken;
    private long userTimeTakenMillis;
    private boolean filterApplied;
    private String info;
    private long userScore;
    private double precisionUserScore;
    private boolean attempted;
    private List<TestSeriesTypeDto> testSeriesTypes;
    private List<List<String>> instructions;
    private String lockMessage;
    private boolean filterApplicable;
    private boolean board;
    private boolean evaluationInProgress;

    // Required---
    private int progress;
    private int totalQuestions;
    private boolean lock;
    private String totalTestGiven;
    private int averageScore;
    private int numberQuestionsAttemptedByUser;

    private boolean newTest;
    private boolean premium;
    private String iconUrl;
    private boolean singleAttempt;
    private String testDate;
    private String testStartTime;
    private String testEndTime;
    private Integer allowEntryWithinMinutes;
    private boolean timeRestrictedEntry;
    private Map<String,String> waitingDialogContent;
    private Map<String,String> testExpiredDialogContent;
    private boolean adaptiveTestSeries;

    protected TestSeriesSetDto(Parcel in) {
        id = in.readString();
        syllabusInfo = in.readString();
        testSeriesId = in.readString();
        expanded = in.readByte() != 0;
        testSeriesName = in.readString();
        totalScore = in.readLong();
        totalTimeToAttempt = in.readLong();
        userTimeTaken = in.readLong();
        userTimeTakenMillis = in.readLong();
        filterApplied = in.readByte() != 0;
        info = in.readString();
        userScore = in.readLong();
        precisionUserScore = in.readDouble(); // Add this
        attempted = in.readByte() != 0;
        testSeriesTypes = in.createTypedArrayList(TestSeriesTypeDto.CREATOR);

        int instructionsSize = in.readInt();
        if (instructionsSize > 0) {
            instructions = new ArrayList<>(instructionsSize);
            for (int i = 0; i < instructionsSize; i++) {
                List<String> innerList = in.createStringArrayList();
                instructions.add(innerList);
            }
        } else {
            instructions = null;
        }

        lockMessage = in.readString();
        filterApplicable = in.readByte() != 0;
        board = in.readByte() != 0;
        evaluationInProgress = in.readByte() != 0;
        progress = in.readInt();
        totalQuestions = in.readInt();
        lock = in.readByte() != 0;
        totalTestGiven = in.readString();
        averageScore = in.readInt();
        numberQuestionsAttemptedByUser = in.readInt();
        newTest = in.readByte() != 0;
        premium = in.readByte() != 0;
        iconUrl = in.readString();
        singleAttempt = in.readByte() != 0; // Add this
        testDate = in.readString();
        testStartTime = in.readString();
        testEndTime = in.readString();
//        allowEntryWithinMinutes = in.readInt();

        int temp = in.readInt();
        allowEntryWithinMinutes = temp == -1 ? null : temp;

        timeRestrictedEntry = in.readByte() != 0;

        waitingDialogContent = new java.util.HashMap<>();
        int waitingSize = in.readInt();
        for (int i = 0; i < waitingSize; i++) {
            String key = in.readString();
            String value = in.readString();
            waitingDialogContent.put(key, value);
        }

        testExpiredDialogContent = new java.util.HashMap<>();
        int expiredSize = in.readInt();
        for (int i = 0; i < expiredSize; i++) {
            String key = in.readString();
            String value = in.readString();
            testExpiredDialogContent.put(key, value);
        }

        adaptiveTestSeries = in.readByte() != 0; // Add this
    }

    public static final Creator<TestSeriesSetDto> CREATOR = new Creator<TestSeriesSetDto>() {
        @Override
        public TestSeriesSetDto createFromParcel(Parcel in) {
            return new TestSeriesSetDto(in);
        }

        @Override
        public TestSeriesSetDto[] newArray(int size) {
            return new TestSeriesSetDto[size];
        }
    };

    @Override
    public String toString() {
        return "TestSeriesSetDto{" +
                "id='" + id + '\'' +
                ", testSeriesId='" + testSeriesId + '\'' +
                ", expanded=" + expanded +
                ", isAttempted='" + attempted + '\'' +
                ", testSeriesName='" + testSeriesName + '\'' +
                ", testDate='" + testDate + '\'' +
                ", testStartTime='" + testStartTime + '\'' +
                ", testEndTime='" + testEndTime + '\'' +
                ", allowEntryWithinMinutes=" + allowEntryWithinMinutes +
                ", timeRestrictedEntry=" + timeRestrictedEntry +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(syllabusInfo);
        dest.writeString(testSeriesId);
        dest.writeByte((byte) (expanded ? 1:0));
        dest.writeString(testSeriesName);
        dest.writeLong(totalScore);
        dest.writeLong(totalTimeToAttempt);
        dest.writeLong(userTimeTaken);
        dest.writeLong(userTimeTakenMillis);
        dest.writeByte((byte) (filterApplied ? 1:0));
        dest.writeString(info);
        dest.writeLong(userScore);
        dest.writeDouble(precisionUserScore); // Add this
        dest.writeByte((byte) (attempted ? 1:0));
        dest.writeTypedList(testSeriesTypes);
        if (instructions != null) {
            dest.writeInt(instructions.size());
            for (List<String> innerList : instructions) {
                dest.writeStringList(innerList);
            }
        } else {
            dest.writeInt(0);
        }

        dest.writeString(lockMessage);
        dest.writeByte((byte) (filterApplicable ? 1:0));
        dest.writeByte((byte) (board ? 1:0));
        dest.writeByte((byte) (evaluationInProgress ? 1:0));
        dest.writeInt(progress);
        dest.writeInt(totalQuestions);
        dest.writeByte((byte) (lock ? 1:0));
        dest.writeString(totalTestGiven);
        dest.writeInt(averageScore);
        dest.writeInt(numberQuestionsAttemptedByUser);
        dest.writeByte((byte) (newTest ? 1:0));
        dest.writeByte((byte) (premium ? 1:0));
        dest.writeString(iconUrl);
        dest.writeByte((byte) (singleAttempt ? 1:0)); // Add this
        dest.writeString(testDate);
        dest.writeString(testStartTime);
        dest.writeString(testEndTime);
        dest.writeInt(allowEntryWithinMinutes != null ? allowEntryWithinMinutes : -1);
        dest.writeByte((byte) (timeRestrictedEntry ? 1 : 0));

        dest.writeInt(waitingDialogContent != null ? waitingDialogContent.size() : 0);
        if (waitingDialogContent != null) {
            for (Map.Entry<String, String> entry : waitingDialogContent.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }

        dest.writeInt(testExpiredDialogContent != null ? testExpiredDialogContent.size() : 0);
        if (testExpiredDialogContent != null) {
            for (Map.Entry<String, String> entry : testExpiredDialogContent.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }

        dest.writeByte((byte) (adaptiveTestSeries ? 1 : 0)); // Add this
    }
}
