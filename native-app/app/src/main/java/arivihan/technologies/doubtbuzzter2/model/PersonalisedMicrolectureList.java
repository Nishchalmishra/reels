package arivihan.technologies.doubtbuzzter2.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PersonalisedMicrolectureList {
    private Map<String, ArrayList<MicroLectureInfoDto>> microlectureContentList;
}
