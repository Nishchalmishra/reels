package arivihan.technologies.doubtbuzzter2.model;
/*
 * File: TestSeriesTypeDto Copyright 1/6/22, 3:08 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 1/6/22, 3:08 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSeriesTypeDto implements Serializable, Parcelable {

    private String id;
    private String testSeriesTypeId;
    private long totalScore;
    private long totalTimeToAttempt;
    private long userScore;
    private double precisionUserScore;
    private long userTimeTaken;
    private boolean attempted;
    private String subject;
    private String info;
    private long totalQuestion;
    private Map<String, Integer> limit;
    private List<TestSeriesQuestionDto> testSeriesQuestionList;

    protected TestSeriesTypeDto(Parcel in) {
        id = in.readString();
        testSeriesTypeId = in.readString();
        totalScore = in.readLong();
        totalTimeToAttempt = in.readLong();
        userScore = in.readLong();
        precisionUserScore = in.readDouble();
        userTimeTaken = in.readLong();
        attempted = in.readByte() != 0;
        subject = in.readString();
        info = in.readString();
        totalQuestion = in.readLong();

        int mapSize = in.readInt();
        if (mapSize > 0) {
            limit = new HashMap<>(mapSize);
            for (int i = 0; i < mapSize; i++) {
                String key = in.readString();
                Integer value = in.readInt();
                limit.put(key, value);
            }
        } else {
            limit = null;
        }

        testSeriesQuestionList =
                in.createTypedArrayList(TestSeriesQuestionDto.CREATOR);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(testSeriesTypeId);
        dest.writeLong(totalScore);
        dest.writeLong(totalTimeToAttempt);
        dest.writeLong(userScore);
        dest.writeDouble(precisionUserScore);
        dest.writeLong(userTimeTaken);
        dest.writeByte((byte) (attempted ? 1 : 0));
        dest.writeString(subject);
        dest.writeString(info);
        dest.writeLong(totalQuestion);

        if (limit != null) {
            dest.writeInt(limit.size());
            for (Map.Entry<String, Integer> entry : limit.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeInt(entry.getValue());
            }
        } else {
            dest.writeInt(0);
        }

        // ---- List<TestSeriesQuestionDto> ----
        dest.writeTypedList(testSeriesQuestionList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TestSeriesTypeDto> CREATOR =
            new Creator<TestSeriesTypeDto>() {
                @Override
                public TestSeriesTypeDto createFromParcel(Parcel in) {
                    return new TestSeriesTypeDto(in);
                }

                @Override
                public TestSeriesTypeDto[] newArray(int size) {
                    return new TestSeriesTypeDto[size];
                }
            };
}

