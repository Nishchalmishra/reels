package arivihan.technologies.doubtbuzzter2.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;

@Dao
public interface MicroLectureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MicroLectureEntity microLecture);

    @Query("SELECT * FROM micro_lecture_new_table WHERE chapterId = :chapterId")
    LiveData<MicroLectureEntity> getSelectionById(String chapterId);

    @Query("DELETE FROM micro_lecture_new_table WHERE userId = :userId")
    void clearTable(String userId);

    @Query("UPDATE micro_lecture_new_table SET selectedMicroLectureId = :selectedMicroLectureId, " +
            "isLectureSelected = :isLectureSelected, isPracticeSelected = :isPracticeSelected, " +
            "typeSelected = :lectureContentType" +
            " WHERE chapterId = :chapterId")
    void updateSelection(String selectedMicroLectureId, boolean isLectureSelected,
                         boolean isPracticeSelected, String chapterId, String lectureContentType);
}
