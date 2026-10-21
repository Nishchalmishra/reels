package arivihan.technologies.doubtbuzzter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardLectureExpandedModel {

    int progress;
    String title;
    MicroLectureContentDto microLectureContentDto;
}
