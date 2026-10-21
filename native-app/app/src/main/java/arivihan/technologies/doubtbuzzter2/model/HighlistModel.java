package arivihan.technologies.doubtbuzzter2.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighlistModel {

    public String id;
    public String videoType;
    public String imageUrl;
    public int progress;
    public String videoSubtitle;
    public String videoTitle;
    public String videoUrl;

}
