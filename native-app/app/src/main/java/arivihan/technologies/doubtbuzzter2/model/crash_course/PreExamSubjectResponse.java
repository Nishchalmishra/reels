package arivihan.technologies.doubtbuzzter2.model.crash_course;

public class PreExamSubjectResponse {
    private String name;
    private String chapterCode;
    private String subjectCode;
    private String subjectId;
    private String contentType;
    private String iconType;
    private String boardCode;
    private String chapterId;

    public PreExamSubjectResponse() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChapterCode() { return chapterCode; }
    public void setChapterCode(String chapterCode) { this.chapterCode = chapterCode; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public String getBoardCode() { return boardCode; }
    public void setBoardCode(String boardCode) { this.boardCode = boardCode; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
}
