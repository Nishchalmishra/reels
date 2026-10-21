package arivihan.technologies.doubtbuzzter2.model.competitive;


import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.HighlistModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ViralVideoResponseDto implements Serializable {
    private String title;
    private String btnText;
    private List<HighlistModel> videos;
}
