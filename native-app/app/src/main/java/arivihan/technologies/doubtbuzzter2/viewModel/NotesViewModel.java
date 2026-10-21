package arivihan.technologies.doubtbuzzter2.viewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.repository.NotesRepository;

public class NotesViewModel extends AndroidViewModel {
    private final NotesRepository repository;

    public NotesViewModel(@NonNull Application application) {
        super(application);
        repository = new NotesRepository(application);
    }

    public void insert(NoteEntity notes) {
        repository.insert(notes);
    }

    public LiveData<Integer> getNotesCount(String userId) {
        return repository.getNotesCount(userId);
    }

    public LiveData<List<NoteEntity>> getAllNotes(String userId) {
        return repository.getAllNotes(userId);
    }

    public LiveData<List<NoteEntity>> getAllNotesBySubjectName(String userId, String subjectName) {
        return repository.getAllNotesBySubjectName(userId, subjectName);
    }

    public LiveData<List<NoteEntity>> getAllNotesBySubjectId(String userId, String subjectId) {
        return repository.getAllNotesBySubjectId(userId, subjectId);
    }

    public LiveData<List<NoteDto>> getAllDownloadedNote(String userId, String downloadStatus) {
        return repository.getAllDownloadedNote(userId, downloadStatus);
    }

    public LiveData<List<String>> getUniqueSubjectNames(String userId) {
        return repository.getUniqueSubjectNames(userId);
    }

    public LiveData<List<String>> getUniqueSubjectIds(String userId) {
        return repository.getUniqueSubjectIds(userId);
    }

    public NoteEntity getNotesById(String id) {
        return repository.getNotesById(id);
    }
    public NoteEntity getNotesByUrl(String url) {
        return repository.getNotesByUrl(url);
    }

    public LiveData<NoteEntity> getLiveNotesById(String id) {
        return repository.getLiveNotesById(id);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public void deleteByLocalPath(String path) {
        repository.deleteByLocalPath(path);
    }

    public void clearTable(String userId) {
        repository.clearTable(userId);
    }

    public void updateStatus(String id, String status) {
        repository.updateStatus(id, status);
    }

    public void updateProgress(int progress, String id) {
        repository.updateProgress(progress, id);
    }

    public void updateLockStatus(String id, boolean locked) {
        repository.updateLockStatus(id, locked);
    }

    public void downloadNotes(Context context, NoteEntity notes) {
        repository.downloadNotes(context, notes);
    }

    public LiveData<NoteEntity> observeNoteById(String id) {
        return repository.observeNoteById(id);
    }

    public void updateFileMeta(String id, long size, int pageCount, String path) {
        repository.updateFileMeta(id, size, pageCount, path);
    }

    public void resetSelection() {
        repository.resetSelection();
    }

    public void selectNote(String id) {
        repository.selectNote(id);
    }
}
