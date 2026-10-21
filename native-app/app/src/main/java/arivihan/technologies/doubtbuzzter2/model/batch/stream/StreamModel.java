package arivihan.technologies.doubtbuzzter2.model.batch.stream;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamModel implements Parcelable, Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("code")
    public String code;

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("shortName")
    public String shortName;

    @SerializedName("subjectsIncluded")
    public String subjectsIncluded;

    @SerializedName("displayOrder")
    public int displayOrder;

    @SerializedName("iconUrl")
    public String iconUrl;

    @SerializedName("selected")
    public boolean selected;

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean Selected){
        this.selected = Selected;
    }
    // local UI fields

    public int selectedPosition;

    protected StreamModel(Parcel in) {
        id = in.readInt();
        code = in.readString();
        displayName = in.readString();
        shortName = in.readString();
        subjectsIncluded = in.readString();
        displayOrder = in.readInt();
        selected = in.readByte() != 0;
        selectedPosition = in.readInt();
    }

    public static final Creator<StreamModel> CREATOR = new Creator<StreamModel>() {
        @Override
        public StreamModel createFromParcel(Parcel in) {
            return new StreamModel(in);
        }

        @Override
        public StreamModel[] newArray(int size) {
            return new StreamModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(code);
        parcel.writeString(displayName);
        parcel.writeString(shortName);
        parcel.writeString(subjectsIncluded);
        parcel.writeInt(displayOrder);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeInt(selectedPosition);
    }
}
