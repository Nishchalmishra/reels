package arivihan.technologies.doubtbuzzter2.model.crash_course;

import java.util.List;

public class PreExamPreparationResponse {
    private String examId;
    private String language;
    private String batchCode;
    private String batchAddOnCode;
    private List<PreExamContentResponse> preExamContentList;
    private List<PreExamSubjectResponse> preExamSubjectList;

    public PreExamPreparationResponse() {}

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getBatchAddOnCode() { return batchAddOnCode; }
    public void setBatchAddOnCode(String batchAddOnCode) { this.batchAddOnCode = batchAddOnCode; }

    public List<PreExamContentResponse> getPreExamContentList() { return preExamContentList; }
    public void setPreExamContentList(List<PreExamContentResponse> preExamContentList) { this.preExamContentList = preExamContentList; }

    public List<PreExamSubjectResponse> getPreExamSubjectList() { return preExamSubjectList; }
    public void setPreExamSubjectList(List<PreExamSubjectResponse> preExamSubjectList) { this.preExamSubjectList = preExamSubjectList; }
}
