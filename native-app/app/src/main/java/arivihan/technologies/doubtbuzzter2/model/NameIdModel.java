package arivihan.technologies.doubtbuzzter2.model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameIdModel implements Parcelable, Serializable {
    public String id;
    public String name;
    private String boardCode;
    public boolean selected;
    public int selectedPosition;
    private List<NameAndIdDto> subTypeList;
    private String nextCardOnClick;
    private boolean triggerClassConfirmationPopup;

    public NameIdModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected NameIdModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        boardCode = in.readString();
        selected = in.readByte() != 0;
        triggerClassConfirmationPopup = in.readByte() != 0;
        selectedPosition = in.readInt();
        nextCardOnClick = in.readString();
    }
    public String getBoardCode() {
        return boardCode;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public static final Creator<NameIdModel> CREATOR = new Creator<NameIdModel>() {
        @Override
        public NameIdModel createFromParcel(Parcel in) {
            return new NameIdModel(in);
        }

        @Override
        public NameIdModel[] newArray(int size) {
            return new NameIdModel[size];
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
        parcel.writeString(boardCode);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeByte((byte) (triggerClassConfirmationPopup ? 1 : 0));
        parcel.writeInt(selectedPosition);
        parcel.writeString(nextCardOnClick);
    }
}
