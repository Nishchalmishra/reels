package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSyllabusOrder implements Parcelable {
    private String chapterId;
    private String subject;
    private String chapterName;
    private boolean selected;
    private boolean enabled;
    private int order;

    protected UserSyllabusOrder(Parcel in) {
        chapterId = in.readString();
        subject = in.readString();
        chapterName = in.readString();
        selected = in.readByte() != 0;
        enabled = in.readByte() != 0;
        order = in.readInt();
    }

    public static final Creator<UserSyllabusOrder> CREATOR = new Creator<UserSyllabusOrder>() {
        @Override
        public UserSyllabusOrder createFromParcel(Parcel in) {
            return new UserSyllabusOrder(in);
        }

        @Override
        public UserSyllabusOrder[] newArray(int size) {
            return new UserSyllabusOrder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(chapterId);
        parcel.writeString(subject);
        parcel.writeString(chapterName);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeByte((byte) (enabled ? 1 :0));
        parcel.writeInt(order);
    }

    @Override
    public String toString() {
        return "UserSyllabusOrder{" +
                "chapterId='" + chapterId + '\'' +
                ", subject='" + subject + '\'' +
                ", chapterName='" + chapterName + '\'' +
                ", selected=" + selected +
                ", enabled=" + enabled +
                ", order=" + order +
                '}';
    }
}
