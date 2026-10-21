package arivihan.technologies.doubtbuzzter2.model;

import java.io.File;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotesDoubtRequestBody implements Serializable {
    private String userId;
    private String phoneNumber;
    private String pdfUrl;
    private int pageNumber;
    private String subject;
    private String chapterName;
    private String studentClass;
    private String examType;
    private String typeOfNotes;
    private String language;
}
