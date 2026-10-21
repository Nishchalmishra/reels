package arivihan.technologies.doubtbuzzter2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;

@Entity(tableName = "micro_lecture_new_table")
@Data
@Builder(access = lombok.AccessLevel.PUBLIC)
public class MicroLectureEntity {
    @PrimaryKey
    @NonNull
    private String chapterId;
    private String selectedMicroLectureId;
    private String userId;

    @Nullable // This column is added by MIGRATION_8_9
    @ColumnInfo(name = "typeSelected")
    private String typeSelected;

    private boolean isLectureSelected;
    private boolean isPracticeSelected;// deprecate do no

    public MicroLectureEntity(@NonNull String chapterId, String selectedMicroLectureId,
                              String userId, @Nullable String typeSelected,
                              boolean isLectureSelected, boolean isPracticeSelected) {
        this.chapterId = chapterId;
        this.selectedMicroLectureId = selectedMicroLectureId;
        this.userId = userId;
        this.isLectureSelected = isLectureSelected;
        this.isPracticeSelected = isPracticeSelected;
        this.typeSelected = typeSelected;
    }


}
