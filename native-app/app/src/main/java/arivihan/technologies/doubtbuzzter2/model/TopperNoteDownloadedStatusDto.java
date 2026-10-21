package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TopperNoteDownloadedStatusDto implements Serializable {
    private String title;
    private String subTitle;
    private String titleImg;
    private String className;
    private String activityParams;
}