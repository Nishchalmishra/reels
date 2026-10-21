package arivihan.technologies.doubtbuzzter2.repository;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.database.dao.NotesDao;
import arivihan.technologies.doubtbuzzter2.database.AppDatabase;
import arivihan.technologies.doubtbuzzter2.helper.FileDownloadHelper;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;

public class NotesRepository {
    private final NotesDao notesDao;
    private final ExecutorService executorService;

    public NotesRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        notesDao = database.notesDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(NoteEntity notes) {
        executorService.execute(() -> notesDao.insert(notes));
    }

    public LiveData<Integer> getNotesCount(String userId) {
        return notesDao.getNotesCount(userId);
    }

    public LiveData<List<NoteEntity>> getAllNotes(String userId) {
        return notesDao.getAllNotes(userId);
    }

    public LiveData<List<NoteEntity>> getAllNotesBySubjectName(String userId, String subjectName) {
        return notesDao.getAllNotesBySubjectName(userId, subjectName);
    }

    public LiveData<List<NoteEntity>> getAllNotesBySubjectId(String userId, String subjectId) {
        return notesDao.getAllNotesBySubjectId(userId, subjectId);
    }

    public NoteEntity getNotesById(String id) {
        return notesDao.getNotesById(id);
    }
    public NoteEntity getNotesByUrl(String url) {
        return notesDao.getNotesByUrl(url);
    }

    public LiveData<NoteEntity> getLiveNotesById(String id) {
        return notesDao.getLiveNotesById(id);
    }

    public LiveData<NoteEntity> observeNoteById(String id) {
        return notesDao.observeNoteById(id);
    }

    public LiveData<List<NoteDto>> getAllDownloadedNote(String userId, String downloadStatus) {
        return notesDao.getAllDownloadedNote(userId, downloadStatus);
    }

    public LiveData<List<String>> getUniqueSubjectNames(String userId) {
        return notesDao.getUniqueSubjectNames(userId);
    }

    public LiveData<List<String>> getUniqueSubjectIds(String userId) {
        return notesDao.getUniqueSubjectIds(userId);
    }

    public void deleteById(String id) {
        executorService.execute(() -> notesDao.deleteById(id));
    }

    public void deleteByLocalPath(String path) {
        executorService.execute(() -> notesDao.deleteByLocalPath(path));
    }

    public void clearTable(String userId) {
        executorService.execute(() -> notesDao.clearTable(userId));
    }

    public void updateStatus(String id, String status) {
        executorService.execute(() -> notesDao.updateStatus(id, status));
    }

    public void updateProgress(int progress, String id) {
        executorService.execute(() -> notesDao.updateProgress(progress, id));
    }

    public void updateLockStatus(String id, boolean locked) {
        executorService.execute(() -> notesDao.updateLockStatus(id, locked));
    }

    public void updateLocalPath(String id, String path) {
        executorService.execute(() -> notesDao.updateLocalPath(id, path));
    }

    public void downloadNotes(Context context, NoteEntity notes) {
        Log.d("", "downloadNotes: " + notes);
        updateStatus(notes.getId(), NoteEntity.STATUS_DOWNLOADING);
        updateProgress(0, notes.getId());

        long downloadId = FileDownloadHelper.startDownload(context,
                notes.getNotesUrl(),
                FileDownloadHelper.buildSafeFileName(notes.getChapterTitle(), notes.getNotesUrl()),
                notes.getChapterTitle());

        executorService.execute(() -> {
            boolean downloading = true;
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);

            while (downloading) {
                Cursor cursor = dm.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytesTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (bytesTotal > 0) {
                        int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                        updateProgress(progress, notes.getId());
                    }

                    int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        String localPath = FileDownloadHelper.getAppDownloadsDirFile(context,
                                FileDownloadHelper.buildSafeFileName(notes.getChapterTitle(), notes.getNotesUrl())).getAbsolutePath();
                        updateStatus(notes.getId(), NoteEntity.STATUS_COMPLETED);
                        updateProgress(100, notes.getId());
                        updateLocalPath(notes.getId(), localPath);
                        downloading = false;
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        updateStatus(notes.getId(), NoteEntity.STATUS_FAILED);
                        downloading = false;
                    }
                    cursor.close();
                }
            }
        });

    }

    public void updateFileMeta(String id, long size, int pageCount, String path) {
        executorService.execute(() -> notesDao.updateFileMeta(id, size, pageCount, path));
    }

    public void resetSelection() {
        executorService.execute(notesDao::resetSelection);
    }

    public void selectNote(String id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            notesDao.resetSelection();
            notesDao.selectNote(id);
        });
//        executorService.execute(() -> notesDao.selectNote(id));
    }

}
