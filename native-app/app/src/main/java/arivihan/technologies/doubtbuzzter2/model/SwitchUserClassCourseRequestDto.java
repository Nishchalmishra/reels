package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.model.batch.home.BottomSheetContentResponse;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SwitchUserClassCourseRequestDto implements Serializable {
    private String courseId;
    private String classId;
    private String switchText;
    private boolean autoSwitchToNEET;
    private BottomSheetContentResponse bottomSheetContentEntity;
}
