package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterType;
import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChapterNameModel implements Serializable {
    private String id;
    private String imageUrl;
    private boolean newLecture;
    private String chapter;
    private String name;
    private boolean locked;
    private String lockMessage;
    private boolean completed;
    private String teacherIntroImage;
    private String notesUrl;
    private boolean board;
    private boolean unwatched;
    private String type;
    private String chapterType;
    private String chapterStatus;
    private String chapterCode;
    private ChapterLiveDto chapterLiveDto;
    private String rescheduledMessage;
    private String subjectCode;
    private String subjectName;
    /**
     * Subject group name for hierarchical chapter grouping.
     * E.g., when Science in 10th is clicked, chapters will have subjectGroupName
     * as "Physics", "Chemistry", or "Biology" for UI tab/grouping.
     */
    private String subjectGroupName;

    /**
     * Subject group ID for the child subject this chapter belongs to.
     * Used when the parent subject has child subjects (e.g., Science → Physics/Chemistry/Biology).
     */
    private String subjectGroupId;

}

