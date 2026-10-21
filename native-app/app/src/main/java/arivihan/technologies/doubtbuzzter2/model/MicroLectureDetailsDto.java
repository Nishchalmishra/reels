package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MicroLectureDetailsDto implements Serializable {
    private ChapterDetailsDto chapterDetails;
    private List<MicroLectureDto> microLectureDtos;
}