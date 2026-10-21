package arivihan.technologies.doubtbuzzter2.model;


import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressDto implements Serializable {

    private String chapterName;
    private int progress;
    private int microlectureCovered;
    private int totalMicrolecture;

}
