package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class subjectProgressDtoList {

    private String subjectName;
    private int progress;
    private int totalChapter;
    private int chapterCovered;

}
