package arivihan.technologies.doubtbuzzter2.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TopicModel extends MicroLectureContentDto {

    public String noOfLecture;
    public String noOfQuestions;
    private String  progress_value;
    private Boolean expandable;
    private Boolean locked;
    private ArrayList<MicroLectureContentDto> questionsDtos;
    private ArrayList<MicroLectureContentDto> lecturesDtos;
    private Boolean isNotWatched;
    private String lockMessage;

}

