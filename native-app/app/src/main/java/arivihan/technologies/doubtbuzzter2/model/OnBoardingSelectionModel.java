package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnBoardingSelectionModel {
    private String name;
    private String id;
    private String batchCode;
    private String examCode;
    private String streamCode;
    private String gradeCode;
    private String yearCode;
    private String boardCode;
    private String courseId;
    private boolean fullWidth;
    // UI Variables
    private String languageCode;

    public String getExamCode() {
        return examCode;
    }
    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }
    public String getGradeCode() {
        return gradeCode;
    }
    public void setGradeCode(String gradeCode) {
        this.gradeCode = gradeCode;
    }
    public String getStreamCode() {
        return streamCode;
    }
    public void setStreamCode(String streamCode) {
        this.streamCode = streamCode;
    }
    public String getBoardCode() {
        return boardCode;
    }
    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }
    public String getCourseId() {
        return courseId;
    }
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    public String getYearCode() {
        return yearCode;
    }
    public void setYearCode(String yearCode) {
        this.yearCode = yearCode;
    }
    public String getBatchCode() {
        return batchCode;
    }
    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public boolean isFullWidth() {
        return fullWidth;
    }

    public void setFullWidth(boolean fullWidth) {
        this.fullWidth = fullWidth;
    }

    public String getLanguageCode() {
        return languageCode;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
