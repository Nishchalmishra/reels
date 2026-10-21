package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: TestSeriesAnalytics Copyright 20/1/22 8:27 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 20/1/22 8:27 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.android.gms.common.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSeriesAnalytics implements Serializable, Parcelable {
    private long totalScore;
    private long score;
    private long timeTaken;
    private String quotes;
    private long rank;
    private int noOfQuestion;
    private int attempted;
    private int unattempted;
    private int correct;
    private int wrong;
    private List<TestSeriesQuestionDto> testSeriesQuestionDtoList;

    public TestSeriesAnalytics() {

    }

    protected TestSeriesAnalytics(Parcel in) {
        totalScore = in.readLong();
        score = in.readLong();
        timeTaken = in.readLong();
        quotes = in.readString();
        rank = in.readLong();
        noOfQuestion = in.readInt();
        attempted = in.readInt();
        unattempted = in.readInt();
        correct = in.readInt();
        wrong = in.readInt();
        testSeriesQuestionDtoList = in.createTypedArrayList(TestSeriesQuestionDto.CREATOR);
    }

    public static final Creator<TestSeriesAnalytics> CREATOR = new Creator<TestSeriesAnalytics>() {
        @Override
        public TestSeriesAnalytics createFromParcel(Parcel in) {
            return new TestSeriesAnalytics(in);
        }

        @Override
        public TestSeriesAnalytics[] newArray(int size) {
            return new TestSeriesAnalytics[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(totalScore);
        dest.writeLong(score);
        dest.writeLong(timeTaken);
        dest.writeString(quotes);
        dest.writeLong(rank);
        dest.writeInt(noOfQuestion);
        dest.writeInt(attempted);
        dest.writeInt(unattempted);
        dest.writeInt(correct);
        dest.writeInt(wrong);
        dest.writeTypedList(testSeriesQuestionDtoList);
    }
}
