package arivihan.technologies.doubtbuzzter2.model.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AcademicBatchResponse implements Serializable {

    @SerializedName("batchId")
    private Integer batchId;

    @SerializedName("batchCode")
    private String batchCode;

    @SerializedName("language")
    private String language;

    @SerializedName("examId")
    private Integer examId;

    @SerializedName("examCode")
    private String examCode;

    @SerializedName("gradeId")
    private Integer gradeId;

    @SerializedName("gradeCode")
    private String gradeCode;

    @SerializedName("streamId")
    private Integer streamId;

    @SerializedName("streamCode")
    private String streamCode;

    @SerializedName("yearId")
    private Integer yearId;

    @SerializedName("yearCode")
    private String yearCode;

    @SerializedName("active")
    private boolean active;

    public Integer getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getExamId() {
        return examId;
    }

    public String getExamCode() {
        return examCode;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public String getGradeCode() {
        return gradeCode;
    }

    public Integer getStreamId() {
        return streamId;
    }

    public String getStreamCode() {
        return streamCode;
    }

    public Integer getYearId() {
        return yearId;
    }

    public String getYearCode() {
        return yearCode;
    }

    public boolean isActive() {
        return active;
    }
}