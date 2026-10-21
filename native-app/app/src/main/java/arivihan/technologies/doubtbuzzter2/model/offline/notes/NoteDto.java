package arivihan.technologies.doubtbuzzter2.model.offline.notes;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoteDto implements Serializable {
    private String id;
    protected String notesUrl;
    private boolean locked;
    private String noteType;
    private String subjectName;
}
