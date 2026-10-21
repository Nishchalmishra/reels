package arivihan.technologies.doubtbuzzter2.model;


import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectModel implements Serializable {

    public String id;
    public String subjectName;
    public String color;
    public String chapterCount;
    private String subjectCode;
    private boolean toShowInConceptual;
    public String iconUrl;
    private String title;
    private boolean custom;
    private boolean personalised;
    private  boolean onDemand;
    private boolean unwatched;
    private boolean selected=true;

    private String backgroundColor;
    private String backgroundImageUrl;
    private String subtitle;
    private String learningPathId;
    private int batchPathId;
    private String miniVideoUrl;
    private String subjectTitleColor;
    private int pos;
    public String getSubjectName() {
        if (StringUtils.isNotEmpty(subjectName)) {
            return subjectName;
        }
        return title;
    }
}
