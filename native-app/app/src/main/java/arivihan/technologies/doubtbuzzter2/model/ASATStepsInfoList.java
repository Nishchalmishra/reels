package arivihan.technologies.doubtbuzzter2.model;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.asat.ASATStepsInfo;
import lombok.Data;

@Data
public class ASATStepsInfoList {
    String userId;
    String classId;
    String courseId;
    String language;
    int startAtStep;
    List<ASATStepsInfo> infoList;
    String thumbnail;
    String videoUrl;
}
