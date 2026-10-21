package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotesModel {

    private String topicName;
    private String chapterName;
    private String createdBy;
    private String Type;
    private int position;


}
