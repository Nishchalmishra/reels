package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: OptionModel Copyright 14/2/21 1:30 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 14/2/21 1:30 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDto implements Parcelable {
    private String optionText;
    private boolean selected;
    private String answer;
    private String attempted;
    private String optionType; // Either TEXT / IMAGE / NULL

    protected OptionDto(Parcel in) {
        optionText = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<OptionDto> CREATOR = new Creator<OptionDto>() {
        @Override
        public OptionDto createFromParcel(Parcel in) {
            return new OptionDto(in);
        }

        @Override
        public OptionDto[] newArray(int size) {
            return new OptionDto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(optionText);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}
