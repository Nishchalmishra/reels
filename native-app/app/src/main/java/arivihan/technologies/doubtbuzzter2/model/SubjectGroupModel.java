package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
 * File: SubjectGroupModel Copyright 30/08/25, 10:04 am Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 30/08/25, 10:04 am
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubjectGroupModel implements Serializable, Parcelable {
    private String id;
    private String name;
    private String courseId;
    private List<String> subjectIds;
    private boolean selected;
    private String subjectGroupLanguage;
    private String boardId;
    private String boardCode;




    protected SubjectGroupModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        courseId = in.readString();
        subjectIds = in.createStringArrayList();
        selected = in.readByte() != 0;
    }

    public static final Creator<SubjectGroupModel> CREATOR = new Creator<SubjectGroupModel>() {
        @Override
        public SubjectGroupModel createFromParcel(Parcel in) {
            return new SubjectGroupModel(in);
        }

        @Override
        public SubjectGroupModel[] newArray(int size) {
            return new SubjectGroupModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(courseId);
        parcel.writeStringList(subjectIds);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }
}
