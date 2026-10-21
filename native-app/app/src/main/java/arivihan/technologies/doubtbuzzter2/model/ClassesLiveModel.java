package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassesLiveModel {


    private String id;
    private String microlectureId;
    private String videoTitle;
    private String videoSubtitle;
    private int progress;
    private String imageUrl;
    private String videoUrl;
    private String chapterId;
    private Boolean board;
    private boolean locked;


}
