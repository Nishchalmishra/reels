package arivihan.technologies.doubtbuzzter2.model;/*
 * File: CropImageDto Copyright 29/08/24, 6:11 pm Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/08/24, 6:11 pm
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CropImageDto implements Parcelable, Serializable {
    private String url;
    private boolean selected;

    protected CropImageDto(Parcel in) {
        url = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<CropImageDto> CREATOR = new Creator<CropImageDto>() {
        @Override
        public CropImageDto createFromParcel(Parcel in) {
            return new CropImageDto(in);
        }

        @Override
        public CropImageDto[] newArray(int size) {
            return new CropImageDto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }
}
