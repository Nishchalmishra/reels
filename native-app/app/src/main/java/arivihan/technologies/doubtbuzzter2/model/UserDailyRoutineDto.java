package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDailyRoutineDto implements Parcelable {

    private String userId;
    private String chapterId;
    private String testSeriesId;
    private String microLectureId;
    private String microLectureQId;
    private String subjectId;
    private Boolean completed;
    private Date dueDate;
    private Date completedDate;
    private Boolean userSelected;
    private String title;
    private String subtitle;
    private int total;
    private String chapterTitle;
    private String timeInMinute;
    private ToDoContentType toDoContentType;
    private String color;
    private Boolean customChapter;
    private Boolean board;
    private String deeplink;
    private String className;
    private Map<String, Object> activityParams;
    private String startTime;
    private String endTime;
    private String subjectColorHex;
    private Boolean taskCompleted;

    protected UserDailyRoutineDto(Parcel in) {
        userId = in.readString();
        chapterId = in.readString();
        testSeriesId = in.readString();
        microLectureId = in.readString();
        microLectureQId = in.readString();
        subjectId = in.readString();
        byte tmpCompleted = in.readByte();
        completed = tmpCompleted == 0 ? null : tmpCompleted == 1;
        byte tmpUserSelected = in.readByte();
        userSelected = tmpUserSelected == 0 ? null : tmpUserSelected == 1;
        title = in.readString();
        subtitle = in.readString();
        total = in.readInt();
        chapterTitle = in.readString();
        timeInMinute = in.readString();
        color = in.readString();
        byte tmpCustomChapter = in.readByte();
        customChapter = tmpCustomChapter == 0 ? null : tmpCustomChapter == 1;
        deeplink = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        subjectColorHex = in.readString();
        byte completionBoolean = in.readByte();
        taskCompleted = completionBoolean == 0 ? null : completionBoolean == 1;
    }

    public static final Creator<UserDailyRoutineDto> CREATOR = new Creator<UserDailyRoutineDto>() {
        @Override
        public UserDailyRoutineDto createFromParcel(Parcel in) {
            return new UserDailyRoutineDto(in);
        }

        @Override
        public UserDailyRoutineDto[] newArray(int size) {
            return new UserDailyRoutineDto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(chapterId);
        parcel.writeString(testSeriesId);
        parcel.writeString(microLectureId);
        parcel.writeString(microLectureQId);
        parcel.writeString(subjectId);
        parcel.writeByte((byte) (completed == null ? 0 : completed ? 1 : 2));
        parcel.writeByte((byte) (userSelected == null ? 0 : userSelected ? 1 : 2));
        parcel.writeString(title);
        parcel.writeString(subtitle);
        parcel.writeInt(total);
        parcel.writeString(chapterTitle);
        parcel.writeString(timeInMinute);
        parcel.writeString(color);
        parcel.writeByte((byte) (customChapter == null ? 0 : customChapter ? 1 : 2));
        parcel.writeString(deeplink);
        parcel.writeString(startTime);
        parcel.writeString(endTime);
        parcel.writeString(subjectColorHex);
    }
}