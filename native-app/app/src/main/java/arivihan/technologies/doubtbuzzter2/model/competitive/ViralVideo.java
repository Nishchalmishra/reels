package arivihan.technologies.doubtbuzzter2.model.competitive;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ViralVideo {
    public String id;
    public String videoOrientation;
    public String imageUrl;
    public String videoUrl;
}
