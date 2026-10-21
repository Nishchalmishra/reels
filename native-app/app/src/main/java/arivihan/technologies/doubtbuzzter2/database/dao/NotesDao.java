package arivihan.technologies.doubtbuzzter2.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;

@Dao
public interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NoteEntity notes);

    @Query("SELECT * FROM notes_table WHERE id = :id")
    NoteEntity getNotesById(String id);

    @Query("SELECT * FROM notes_table WHERE notesUrl = :url")
    NoteEntity getNotesByUrl(String url);

    @Query("SELECT * FROM notes_table WHERE id = :id")
    LiveData<NoteEntity> getLiveNotesById(String id);

    @Query("SELECT COUNT(*) FROM notes_table WHERE userId = :userId")
    LiveData<Integer> getNotesCount(String userId);

    @Query("SELECT * FROM notes_table WHERE userId = :userId")
    LiveData<List<NoteEntity>> getAllNotes(String userId);

    @Query("SELECT * FROM notes_table")
    LiveData<List<NoteEntity>> getAllNotes();

    @Query("SELECT * FROM notes_table WHERE userId = :userId AND subjectName = :subjectName")
    LiveData<List<NoteEntity>> getAllNotesBySubjectName(String userId, String subjectName);

    @Query("SELECT * FROM notes_table WHERE userId = :userId AND subjectId = :subjectId")
    LiveData<List<NoteEntity>> getAllNotesBySubjectId(String userId, String subjectId);

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT id,locked,notesUrl FROM notes_table " +
            "WHERE userId = :userId AND status = :status")
    LiveData<List<NoteDto>> getAllDownloadedNote(String userId, String status);

    @Query("SELECT DISTINCT subjectName FROM notes_table WHERE userId = :userId")
    LiveData<List<String>> getUniqueSubjectNames(String userId);

    @Query("SELECT DISTINCT subjectId FROM notes_table WHERE userId = :userId")
    LiveData<List<String>> getUniqueSubjectIds(String userId);

    @Query("DELETE FROM notes_table WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM notes_table WHERE localPath = :path")
    void deleteByLocalPath(String path);

    @Query("DELETE FROM notes_table WHERE userId = :userId")
    void clearTable(String userId);

    @Query("UPDATE notes_table SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);

    @Query("UPDATE notes_table SET progress = :progress WHERE id = :id")
    void updateProgress(int progress, String id);

    @Query("UPDATE notes_table SET locked = :locked WHERE id = :id")
    void updateLockStatus(String id, boolean locked);

    @Query("UPDATE notes_table SET localPath = :path WHERE id = :id")
    void updateLocalPath(String id, String path);

    @Query("SELECT * FROM notes_table WHERE id = :id")
    LiveData<NoteEntity> observeNoteById(String id);

    @Query("UPDATE notes_table SET fileSize = :size, pageCount = :pageCount, localPath = :path WHERE id = :id")
    void updateFileMeta(String id, long size, int pageCount, String path);

    @Query("UPDATE notes_table SET selected = 0")
    void resetSelection();

    @Query("UPDATE notes_table SET selected = 1 WHERE id = :id")
    void selectNote(String id);
}
