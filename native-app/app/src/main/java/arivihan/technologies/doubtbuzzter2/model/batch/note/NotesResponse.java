package arivihan.technologies.doubtbuzzter2.model.batch.note;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotesResponse {
    private Integer id;
    private String code;
    private String subjectName;
    private String subject;
    private String title;
    private String notesBy;
    private String url;
    private String type;
    private String accessType;
    private Integer displayOrder;
    private Boolean locked;
    private Boolean isActive;
}
