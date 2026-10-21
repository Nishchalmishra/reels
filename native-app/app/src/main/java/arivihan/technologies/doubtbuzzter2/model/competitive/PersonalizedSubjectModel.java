package arivihan.technologies.doubtbuzzter2.model.competitive;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedSubjectModel {
    public String id;
    public String title;
    private String backgroundColor;
    private String backgroundImage;
    private String subtitle;
    private String icon;
}

