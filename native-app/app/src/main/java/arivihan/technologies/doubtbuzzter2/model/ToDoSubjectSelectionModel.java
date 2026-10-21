package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToDoSubjectSelectionModel implements Parcelable {
    public String subjectName;
    private String subjectCode;
    private boolean selected;

    protected ToDoSubjectSelectionModel(Parcel in) {
        subjectName = in.readString();
        subjectCode = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<ToDoSubjectSelectionModel> CREATOR = new Creator<ToDoSubjectSelectionModel>() {
        @Override
        public ToDoSubjectSelectionModel createFromParcel(Parcel in) {
            return new ToDoSubjectSelectionModel(in);
        }

        @Override
        public ToDoSubjectSelectionModel[] newArray(int size) {
            return new ToDoSubjectSelectionModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(subjectName);
        parcel.writeString(subjectCode);
        parcel.writeByte((byte) (selected ? 1 : 0));
    }
}
