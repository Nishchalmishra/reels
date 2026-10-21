package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InteractiveQuestionResponseDto implements Serializable {
    private List<MicroLectureQuestionsAnsDto> questionsAnsDtoList;
    private List<MicroLectureQuestionAnsResponseDto> microLectureQuestionAnsResponseDtos;
}
