package arivihan.technologies.doubtbuzzter2.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.database.dao.MediaDao;
import arivihan.technologies.doubtbuzzter2.database.AppDatabase;
import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import lombok.Getter;

@Getter
public class MediaRepository {
    private final MediaDao mediaDao;

    public LiveData<List<MediaEntity>> getAllMedia(String userId) {
        return mediaDao.getAllMedia(userId);
    }

    public MediaRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        mediaDao = database.mediaDao();
    }

    public void insert(MediaEntity mediaEntity) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.insert(mediaEntity));
    }

    public void delete(String microLectureId) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.deleteById(microLectureId));
    }

    public void deleteThenInsert(MediaEntity mediaEntity) {
        Executors.newSingleThreadExecutor().execute(() ->{
            mediaDao.deleteById(mediaEntity.getMicroLectureId());
            mediaDao.insert(mediaEntity);
        });
    }

    public void deleteByUrl(String url) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.deleteByUrl(url));
    }

    public MediaEntity getMediaById(String id) {
        return mediaDao.getMediaById(id);
    }

    public LiveData<MediaEntity> getMediaByLectureId(String microLectureId) {
        return mediaDao.getMediaByLectureId(microLectureId);
    }

    public void updateById(String id, String status) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.updateStatus(id,status));
    }

    public void updateByLockedStatusByLectureId(String microLectureID, boolean locked) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.
                updateByLockedStatusByLectureId(microLectureID,locked));
    }

    public void clearUserData(String userId) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.clearTable(userId));
    }

    public void updateProgress(int progress, String id) {
        Executors.newSingleThreadExecutor().execute(() -> mediaDao.updateProgress(progress,id));
    }

    public LiveData<List<DownloadMedia>>getAllDownloadedMedia(String userId, String downloadStatus) {
        return mediaDao.getAllDownloadedMedia(userId,downloadStatus);
    }
}
