package arivihan.technologies.doubtbuzzter2.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel;
import arivihan.technologies.doubtbuzzter2.model.batch.stream.StreamModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOnboardingState implements Serializable, Parcelable {
    private String userId;
    private String phoneNumber;
    private String name;
    private String language;
    private String secondaryCourseId;

    private int stepNumber;
    private boolean completed;
    private boolean nextIsEnabled;
    private boolean hindiSelected;
    private boolean englishSelected;
    private boolean jeeSelected;
    private boolean neetSelected;

    private ArrayList<NameIdModel> coachingList;
    private ArrayList<NameIdModel> boardList;
    private ArrayList<NameIdModel> examList;
    private ArrayList<NameIdModel> classList;

    private ArrayList<SubjectGroupModel> subjectGroupModels;
    private ArrayList<AddSubjectAboutFragmentModel> subjectModel;
    private ArrayList<String> subjectIds;

    private ArrayList<GradeModel> gradeList;
    private ArrayList<StreamModel> streamList;

    private String selectedBoardName;
    private String selectedBoard;
    private String selectedBoardCode;
    private String selectedCoachingId;

    private String courseId;
    private String classId;

    private String gradeId;
    private String gradeCode;

    public ArrayList<NameIdModel> getBoardList() {
        return boardList;
    }
    public String getSelectedBoardCode() {
        return selectedBoardCode;
    }

    public ArrayList<NameIdModel> getExamList() {
        return examList;
    }

    public ArrayList<NameIdModel> getClassList() {
        return classList;
    }

    public ArrayList<StreamModel> getStreamList() {
        return streamList;
    }

    public String getSelectedBoard() {
        return selectedBoard;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getClassId() {
        return classId;
    }

    public String getGradeId() {
        return gradeId;
    }

    public String getGradeCode() {
        return gradeCode;
    }
    protected UserOnboardingState(Parcel in) {
        userId = in.readString();
        phoneNumber = in.readString();
        name = in.readString();
        language = in.readString();
        secondaryCourseId = in.readString();
        stepNumber = in.readInt();
        completed = in.readByte() != 0;
        hindiSelected = in.readByte() != 0;
        englishSelected = in.readByte() != 0;
        jeeSelected = in.readByte() != 0;
        nextIsEnabled = in.readByte() != 0;
        neetSelected = in.readByte() != 0;
        boardList = in.createTypedArrayList(NameIdModel.CREATOR);
        examList = in.createTypedArrayList(NameIdModel.CREATOR);
        classList = in.createTypedArrayList(NameIdModel.CREATOR);
        subjectModel = in.createTypedArrayList(AddSubjectAboutFragmentModel.CREATOR);
        subjectIds = in.createStringArrayList();
    }

    public static final Creator<UserOnboardingState> CREATOR = new Creator<UserOnboardingState>() {
        @Override
        public UserOnboardingState createFromParcel(Parcel in) {
            return new UserOnboardingState(in);
        }

        @Override
        public UserOnboardingState[] newArray(int size) {
            return new UserOnboardingState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(phoneNumber);
        parcel.writeString(name);
        parcel.writeString(language);
        parcel.writeString(secondaryCourseId);
        parcel.writeInt(stepNumber);
        parcel.writeByte((byte) (completed ? 1 : 0));
        parcel.writeByte((byte) (hindiSelected ? 1 : 0));
        parcel.writeByte((byte) (nextIsEnabled ? 1 : 0));
        parcel.writeByte((byte) (englishSelected ? 1 : 0));
        parcel.writeByte((byte) (jeeSelected ? 1 : 0));
        parcel.writeByte((byte) (neetSelected ? 1 : 0));
        parcel.writeTypedList(boardList);
        parcel.writeTypedList(examList);
        parcel.writeTypedList(classList);
        parcel.writeTypedList(subjectModel);
        parcel.writeStringList(subjectIds);
    }
}
