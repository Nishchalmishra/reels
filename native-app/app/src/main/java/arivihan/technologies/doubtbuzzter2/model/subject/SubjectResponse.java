package arivihan.technologies.doubtbuzzter2.model.subject;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String subjectName;

    @SerializedName("subjectName")
    public String subjectTitle;

    @SerializedName("code")
    private String subjectCode;

    @SerializedName("iconUrl")
    public String iconUrl;

    @SerializedName("colorCode")
    public String color;

    @SerializedName("displayOrder")
    private Integer displayOrder;

    @SerializedName("isMainSubject")
    private Boolean isMainSubject;

    @SerializedName("isOptional")
    private Boolean isOptional;

    @SerializedName("chapterCount")
    public String chapterCount;

    @SerializedName("selected")
    private boolean selected = true;

    @SerializedName("backgroundImageUrl")
    private String backgroundImageUrl;

    // Fields not in new API but still used in codebase
    private String title;
    private boolean toShowInConceptual;
    private boolean custom;
    private boolean personalised;
    private boolean onDemand;
    private boolean unwatched;
    private String backgroundColor;
    private String subtitle;
    private String learningPathId;
    private int batchPathId;
    private String miniVideoUrl;
    private String subjectTitleColor;
    private int pos;
    private boolean hasChildSubjects;
    private List<SubjectResponse> childSubjects;

    public String getSubjectName() {
        if (StringUtils.isNotEmpty(subjectName)) {
            return subjectName;
        }
        return StringUtils.isNotEmpty(title) ? title : subjectTitle;
    }

    public String getSubjectCode() {
        return StringUtils.isNotEmpty(subjectCode) ? subjectCode : subjectName;
    }
}
