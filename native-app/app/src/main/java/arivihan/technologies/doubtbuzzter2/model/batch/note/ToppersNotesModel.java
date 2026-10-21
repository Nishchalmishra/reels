package arivihan.technologies.doubtbuzzter2.model.batch.note;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ToppersNotesModel implements Parcelable {
    private String id;
    private String subjectId;
    private String subjectName;
    private String chapterTitle;
    private String notesBy;
    private String notesUrl;
    private boolean locked;
    private int pageCount;
    private long fileSize;
    private String fileType;
    private String notesType;

    // Offline internal variables
    private int progress;
    private String status;
    private boolean isDownloadComplete;

    protected ToppersNotesModel(Parcel in) {
        subjectId = in.readString();
        subjectName = in.readString();
        chapterTitle = in.readString();
        notesBy = in.readString();
        notesUrl = in.readString();
        locked = in.readByte() != 0;
        pageCount = in.readInt();
        fileSize = in.readLong();
        fileType = in.readString();
        progress = in.readInt();
        isDownloadComplete = in.readByte() != 0;
        status = in.readString();
        notesType = in.readString();
    }

    public static final Creator<ToppersNotesModel> CREATOR = new Creator<ToppersNotesModel>() {
        @Override
        public ToppersNotesModel createFromParcel(Parcel in) {
            return new ToppersNotesModel(in);
        }

        @Override
        public ToppersNotesModel[] newArray(int size) {
            return new ToppersNotesModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(subjectId);
        parcel.writeString(subjectName);
        parcel.writeString(chapterTitle);
        parcel.writeString(notesBy);
        parcel.writeString(notesUrl);
        parcel.writeByte((byte) (locked ? 1 : 0));
        parcel.writeInt(pageCount);
        parcel.writeLong(fileSize);
        parcel.writeString(fileType);
        parcel.writeInt(progress);
        parcel.writeByte((byte) (isDownloadComplete ? 1 : 0));
        parcel.writeString(status);
        parcel.writeString(notesType);
    }
}
