package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDetailsDto implements Serializable {

    private String id;
    private String name;
    private String title;
    private String notesUrl;
    private String ncertSolutions;
    private NotesDto notesData;
    private boolean board;
    private int progress;
    private String subjectId;
    private String subjectName;
    private String notesType;

}