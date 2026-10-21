package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSubjectAboutFragmentModel implements Parcelable {

    public String id;
    public String code;
    public String name;
    public Boolean additional;
    public String selected;
    public boolean checked;
    protected AddSubjectAboutFragmentModel(Parcel in) {
        id = in.readString();
        code = in.readString();
        name = in.readString();
        byte tmpAdditional = in.readByte();
        additional = tmpAdditional == 0 ? null : tmpAdditional == 1;
        selected = in.readString();
        checked = in.readByte() != 0;
    }

    public static final Creator<AddSubjectAboutFragmentModel> CREATOR = new Creator<AddSubjectAboutFragmentModel>() {
        @Override
        public AddSubjectAboutFragmentModel createFromParcel(Parcel in) {
            return new AddSubjectAboutFragmentModel(in);
        }

        @Override
        public AddSubjectAboutFragmentModel[] newArray(int size) {
            return new AddSubjectAboutFragmentModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(code);
        parcel.writeString(name);
        parcel.writeByte((byte) (additional == null ? 0 : additional ? 1 : 2));
        parcel.writeString(selected);
        parcel.writeByte((byte) (checked ? 1 : 0));
    }
}
