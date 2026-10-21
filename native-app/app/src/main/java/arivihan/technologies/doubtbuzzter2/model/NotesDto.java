package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class NotesDto implements Serializable
{
    private String notesUrl;
    private String notesType;
    private String notesSubjectName;
}
