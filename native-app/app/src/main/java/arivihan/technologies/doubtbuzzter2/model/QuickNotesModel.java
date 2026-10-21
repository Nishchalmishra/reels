package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickNotesModel {

    private String id;
    private boolean fromArivihan;
    private String subject;
    private String title;
    private String subTitle;
    private String addedOn;
    private List<QuickNotesContents> quickNotesContents;

}
