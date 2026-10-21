package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;

@Entity(tableName = "micro_lecture_content_table")
@Data
@Builder(access = lombok.AccessLevel.PUBLIC)
public class MicroLectureContentEntity {
    @PrimaryKey
    @NonNull
    private String chapterId;
    private String selectedMicroLectureId;
    private String userId;
    private String microLectureContentType;

    public MicroLectureContentEntity(@NonNull String chapterId, String selectedMicroLectureId,
                              String userId,String microLectureContentType) {
        this.chapterId = chapterId;
        this.selectedMicroLectureId = selectedMicroLectureId;
        this.userId = userId;
        this.microLectureContentType=getMicroLectureContentType();
    }
}
