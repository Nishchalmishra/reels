package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.model.competitive.subject.TopicWiseChapterListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSelectionDto implements Serializable {
    private String id;
    private String name;
    private boolean selected;
}
