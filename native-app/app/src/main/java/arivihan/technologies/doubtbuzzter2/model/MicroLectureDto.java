package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroLectureDto implements Serializable {

    private String id;
    private String code;
    private String title;
    private boolean completed;
    private boolean videoCompleted;
    private boolean locked;
    private boolean converted;
    private boolean highlightText;
    private VideoPreview videoPreview;
    private VideoDetails videoDetails;
    private List<Questions> questions;
    private boolean playVideo ;
    private String lockMessage;
    private boolean downloaded;
    private boolean microLectureSelected;
    private String downloadStatus;
    private String pdfUrl;
    private boolean pdfViewed;
    private int downloadProgress;
    private String microLectureContentType;
    private long likeCount;
    private Boolean liked;

    private int pdfPageCount;
    private int pdfProgress;
    private String pdfStatus;
    private boolean pdfDownloadComplete;
    private boolean pdfLocked;

    @NonNull
    @Override
    public String toString() {
        return "MicroLectureDto{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", locked='" + locked + '\'' +
                ", lockMessage='" + lockMessage + '\'' +
                ", playVideo=" + playVideo +
                ", microLectureSelected=" + microLectureSelected +
                ", questions =" + questions +
                ", highlightText =" + highlightText +
                ", likeCount = " + likeCount +
                ", liked = " + liked +
                '}';
    }
}
