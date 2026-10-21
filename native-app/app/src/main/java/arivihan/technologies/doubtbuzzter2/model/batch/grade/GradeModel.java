package arivihan.technologies.doubtbuzzter2.model.batch.grade;


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
public class GradeModel implements Parcelable, Serializable {

    @SerializedName("id")
    private Integer id;

    @SerializedName("code")
    public String code;

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("neumericValue")
    public int neumericValue;

    @SerializedName("displayOrder")
    private Integer displayOrder;

    @SerializedName("name")
    private String name;

    @SerializedName("language")
    private String language;

    @SerializedName("examCode")
    private String examCode;

    @SerializedName("examId")
    private Integer examId;

    @SerializedName("gradeId")
    private Integer gradeId;

    @SerializedName("gradeCode")
    private String gradeCode;

    @SerializedName("academicYearId")
    private Integer academicYearId;

    @SerializedName("academicYearCode")
    private String academicYearCode;

    @SerializedName("gradeConfirmationPopup")
    private boolean gradeConfirmationPopup;

    @SerializedName("selected")
    private boolean selected;

    // local UI fields
    public int selectedPosition;


    // ------------------------------------------------------------------------
    // Explicit getters
    // ------------------------------------------------------------------------

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNeumericValue() {
        return neumericValue;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getExamCode() {
        return examCode;
    }

    public Integer getExamId() {
        return examId;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public String getGradeCode() {
        return gradeCode;
    }

    public Integer getAcademicYearId() {
        return academicYearId;
    }

    public String getAcademicYearCode() {
        return academicYearCode;
    }

    public boolean isGradeConfirmationPopup() {
        return gradeConfirmationPopup;
    }

    public boolean getSelected() { return selected; }


    public int getSelectedPosition() {
        return selectedPosition;
    }


    // ------------------------------------------------------------------------
    // Explicit setters
    // ------------------------------------------------------------------------

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setNeumericValue(int neumericValue) {
        this.neumericValue = neumericValue;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public void setGradeCode(String gradeCode) {
        this.gradeCode = gradeCode;
    }

    public void setAcademicYearId(Integer academicYearId) {
        this.academicYearId = academicYearId;
    }

    public void setAcademicYearCode(String academicYearCode) {
        this.academicYearCode = academicYearCode;
    }

    public void setGradeConfirmationPopup(
            boolean gradeConfirmationPopup
    ) {
        this.gradeConfirmationPopup = gradeConfirmationPopup;
    }

    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
    }


    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }


    // ------------------------------------------------------------------------
    // Parcelable
    // ------------------------------------------------------------------------

    protected GradeModel(Parcel in) {
        id = in.readInt();
        code = in.readString();
        displayName = in.readString();
        neumericValue = in.readInt();
        displayOrder = in.readInt();
        name = in.readString();
        language = in.readString();
        examCode = in.readString();
        examId = in.readInt();
        gradeId = in.readInt();
        gradeCode = in.readString();
        academicYearId = in.readInt();
        academicYearCode = in.readString();
        selected = in.readByte() != 0;
        selectedPosition = in.readInt();
    }

    public static final Creator<GradeModel> CREATOR = new Creator<GradeModel>() {
        @Override
        public GradeModel createFromParcel(Parcel in) {
            return new GradeModel(in);
        }

        @Override
        public GradeModel[] newArray(int size) {
            return new GradeModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeInt(id != null ? id : 0);
        parcel.writeString(code);
        parcel.writeString(displayName);
        parcel.writeInt(neumericValue);
        parcel.writeInt(displayOrder != null ? displayOrder : 0);
        parcel.writeString(name);
        parcel.writeString(language);
        parcel.writeString(examCode);
        parcel.writeInt(examId != null ? examId : 0);
        parcel.writeInt(gradeId != null ? gradeId : 0);
        parcel.writeString(gradeCode);
        parcel.writeInt(academicYearId != null ? academicYearId : 0);
        parcel.writeString(academicYearCode);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeInt(selectedPosition);
    }
}
