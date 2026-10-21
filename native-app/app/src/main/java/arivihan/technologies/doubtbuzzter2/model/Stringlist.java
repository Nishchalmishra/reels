package arivihan.technologies.doubtbuzzter2.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stringlist implements Parcelable{

    private List<String> strings;

    protected Stringlist(Parcel in) {
        strings = in.createStringArrayList();
    }

    public static final Parcelable.Creator<Stringlist> CREATOR = new Parcelable.Creator<Stringlist>() {
        @Override
        public Stringlist createFromParcel(Parcel in) {
            return new Stringlist(in);
        }

        @Override
        public Stringlist[] newArray(int size) {
            return new Stringlist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(strings);
    }

}
