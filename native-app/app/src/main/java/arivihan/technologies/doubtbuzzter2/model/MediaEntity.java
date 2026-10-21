package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.converters.Converters;
import lombok.Builder;
import lombok.Data;

@Entity(tableName = "media_table")
@Data
@Builder(access = lombok.AccessLevel.PUBLIC)
public class MediaEntity implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;

    private String url;
    private String title;
    private String chapterTitle;
    private String chapterId;
    private String microLectureTitle;
    private String microLectureId;
    private String duration;
    private String size;
    private int progress;
    private String language;
    private String status;

    @TypeConverters(Converters.class)
    private List<MicroLectureQuestionsAnsDto> questionsAnsList;

    @TypeConverters(Converters.class)
    private List<MicroLectureQuestionAnsResponseDto> questionsAnsResponseList;

    private long startPos;
    private long endPos;
    private String userId;
    private String thumbnailUrl;
    private boolean thumbnailLoaded;

    @ColumnInfo(name = "locked")
    private boolean locked;

    public MediaEntity(@NonNull String id, String url, String title, String chapterTitle, String chapterId,
                       String microLectureTitle, String microLectureId, String duration, String size,
                       int progress, String language, String status, List<MicroLectureQuestionsAnsDto> questionsAnsList,
                       List<MicroLectureQuestionAnsResponseDto> questionsAnsResponseList,
                       long startPos, long endPos,String userId,String thumbnailUrl,boolean thumbnailLoaded,boolean locked) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.chapterTitle = chapterTitle;
        this.chapterId = chapterId;
        this.microLectureTitle = microLectureTitle;
        this.microLectureId = microLectureId;
        this.duration = duration;
        this.size = size;
        this.progress = progress;
        this.language = language;
        this.status = status;
        this.questionsAnsList = questionsAnsList;
        this.startPos = startPos;
        this.endPos = endPos;
        this.questionsAnsResponseList = questionsAnsResponseList;
        this.userId = userId;
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailLoaded =  thumbnailLoaded;
        this.locked = locked;
    }
    @Override
    public String toString() {
        return "MediaEntity{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", chapterTitle='" + chapterTitle + '\'' +
                ", chapterId='" + chapterId + '\'' +
                ", microLectureTitle='" + microLectureTitle + '\'' +
                ", microLectureId='" + microLectureId + '\'' +
                ", duration='" + duration + '\'' +
                ", size='" + size + '\'' +
                ", progress=" + progress +
                ", language='" + language + '\'' +
                ", status='" + status + '\'' +
                ", questionsAnsList=" + questionsAnsList +
                ", questionsAnsResponseList=" + questionsAnsResponseList +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                ", userId='" + userId + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", thumbnailLoaded=" + thumbnailLoaded +
                '}';
    }

}

