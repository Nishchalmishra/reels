package arivihan.technologies.doubtbuzzter2.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DoubtContextDto  {
    //chapter id will come as input
    private String chapterId;
    //microLecture id will come as input
    private String microLectureId;
    //time when doubt asked will come as input
    private long time;
    //doubt text as input
    private String doubtText;
    //This returns the response of doubt asked
    private String result;
    //This indicates response generated successfully or not
    private String message;
    //This is microlecture title used to get the response
    private String title;

    DoubtResponseDto doubtResponseDto;

    @Override
    public String toString() {
        return "DoubtContextDto{" +
                "chapterId='" + chapterId + '\'' +
                ", microLectureId='" + microLectureId + '\'' +
                ", time=" + time +
                ", doubtText='" + doubtText + '\'' +
                ", result='" + result + '\'' +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
