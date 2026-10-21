package arivihan.technologies.doubtbuzzter2.model.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpdateAcademicDetailsRequest implements Serializable {

    @SerializedName("userName")
    private String userName;

    @SerializedName("examId")
    private Integer examId;

    @SerializedName("gradeId")
    private Integer gradeId;

    @SerializedName("streamId")
    private Integer streamId;

    @SerializedName("academicYearId")
    private Integer academicYearId;

    @SerializedName("language")
    private String language;

    public UpdateAcademicDetailsRequest() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    public Integer getStreamId() {
        return streamId;
    }

    public void setStreamId(Integer streamId) {
        this.streamId = streamId;
    }

    public Integer getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Integer academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}