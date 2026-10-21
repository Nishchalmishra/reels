package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularLiveClassesModel {

    public String id;
    public int images;
    public int progress;
    public String videoSubtitle;
    public String videoTitle;
    public String videoUrl;


}
