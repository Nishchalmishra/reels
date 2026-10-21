package arivihan.technologies.doubtbuzzter2.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;

@Dao
public interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaEntity media);

    @Query("SELECT * FROM media_table WHERE microLectureId = :microLectureId")
    LiveData<MediaEntity> getMediaByLectureId(String microLectureId);

    @Query("SELECT microLectureId,locked FROM media_table " +
            "WHERE userId = :userId AND status = :downloadStatus")
    LiveData<List<DownloadMedia>> getAllDownloadedMedia(String userId, String downloadStatus);

    @Query("SELECT * FROM media_table WHERE id = :id")
    MediaEntity getMediaById(String id);

    @Query("DELETE FROM media_table WHERE microLectureId = :microLectureId")
    void deleteById(String microLectureId);

    @Query("DELETE FROM media_table WHERE id = :url")
    void deleteByUrl(String url);

    @Query("UPDATE media_table SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);

    @Query("SELECT * FROM media_table WHERE userId = :userId")
    LiveData<List<MediaEntity>> getAllMedia(String userId);

    @Query("DELETE FROM media_table WHERE userId = :userId")
    void clearTable(String userId);

    @Query("UPDATE media_table SET progress = :progress WHERE id = :id")
    void updateProgress(int progress, String id);

    @Query("UPDATE media_table SET locked= :locked WHERE microLectureId = :microLectureId")
    void updateByLockedStatusByLectureId(String microLectureId, boolean locked);
}
