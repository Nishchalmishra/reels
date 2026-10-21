package arivihan.technologies.doubtbuzzter2.model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SamplePaperModel implements Parcelable {

    private String id;
    private boolean fromArivihan;
    private String subject;
    private String title;
    private String subTitle;
    private String downloadLink;
    private String notesType;
    private boolean locked;

    private int pageCount;
    private long fileSize;

    // Offline internal variables
    private int progress;
    private String status;
    private boolean isDownloadComplete;

    protected SamplePaperModel(Parcel in) {
        id = in.readString();
        fromArivihan = in.readByte() != 0;
        subject = in.readString();
        title = in.readString();
        subTitle = in.readString();
        downloadLink = in.readString();
        notesType = in.readString();
        locked = in.readByte() != 0;
        pageCount = in.readInt();
        fileSize = in.readLong();
        progress = in.readInt();
        status = in.readString();
        isDownloadComplete = in.readByte() != 0;
    }

    public static final Creator<SamplePaperModel> CREATOR = new Creator<SamplePaperModel>() {
        @Override
        public SamplePaperModel createFromParcel(Parcel in) {
            return new SamplePaperModel(in);
        }

        @Override
        public SamplePaperModel[] newArray(int size) {
            return new SamplePaperModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (fromArivihan ? 1 : 0));
        dest.writeString(subject);
        dest.writeString(title);
        dest.writeString(subTitle);
        dest.writeString(downloadLink);
        dest.writeString(notesType);
        dest.writeByte((byte) (locked ? 1 : 0));
        dest.writeInt(pageCount);
        dest.writeLong(fileSize);
        dest.writeInt(progress);
        dest.writeString(status);
        dest.writeByte((byte) (isDownloadComplete ? 1 : 0));
    }

}
