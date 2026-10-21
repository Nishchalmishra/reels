package arivihan.technologies.doubtbuzzter2.model;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyllabusCompetitionModel {

    String chapter;
    String chapterId;
    boolean completed;
    List<NameAndIdDto> microlecture;

}
