package arivihan.technologies.doubtbuzzter2.model.offline.notes;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

import lombok.Data;

@Entity(tableName = "notes_table")
@Data
public class NoteEntity implements Serializable {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_QUEUE = "QUEUE";
    public static final String STATUS_DOWNLOADING = "DOWNLOADING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    @PrimaryKey
    @NonNull
    private String id;
    private String subjectId;
    private String subjectName;
    private String chapterTitle;
    private String notesBy;
    private String notesUrl;
    private boolean locked;
    private int pageCount;
    private long fileSize;
    private String fileType;
    private String status;
    private int progress;
    private String userId;
    private String localPath;
    private String noteType;
    private String language;
    private boolean selected;

    public NoteEntity(@NonNull String id, String subjectId, String subjectName, String chapterTitle,
                      String notesBy, String notesUrl, boolean locked, int pageCount, long fileSize,
                      String fileType, String status, int progress, String localPath, String language) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.chapterTitle = chapterTitle;
        this.notesBy = notesBy;
        this.notesUrl = notesUrl;
        this.locked = locked;
        this.pageCount = pageCount;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.status = status;
        this.progress = progress;
        this.localPath = localPath;
        this.language = language;
    }

    // Written out instead of relying on @Data: Lombok-generated accessors are invisible to
    // the Kotlin compiler, and the Compose topper-notes screen needs these. Lombok skips
    // generating a method that already exists, so @Data still covers the rest.
    @NonNull
    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }
}
