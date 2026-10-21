package arivihan.technologies.doubtbuzzter2.model.chapter.live;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterPopUpDto implements Serializable {
    private String title;
    private String subTitle;
    private String btnName;
}
