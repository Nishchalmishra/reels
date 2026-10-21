package arivihan.technologies.doubtbuzzter2.database.helper;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.NotesResponse;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.SyncDownloadNotesRequest;

/**
 * Helper class for converting between Room DB models (NoteEntity / NoteDto)
 * and the API model (NotesResponse).
 *
 * Key difference: NotesResponse.id is Integer, NoteEntity.id is String.
 * All conversions handle this safely.
 */
public class NotesMapper {

    private static final String TAG = NotesMapper.class.getSimpleName();

    private NotesMapper() {}

    /**
     * Converts a {@link NoteDto} (from Room DB) to a {@link NotesResponse} for the sync request body.
     * String id is safely parsed to Integer; returns null id if parsing fails.
     */
    public static NotesResponse fromNoteDto(NoteDto dto) {
        NotesResponse response = new NotesResponse();
        if (dto.getId() != null) {
            try {
                response.setId(Integer.parseInt(dto.getId()));
            } catch (NumberFormatException e) {
                Log.w(TAG, "fromNoteDto: could not parse id='" + dto.getId() + "' as Integer");
                response.setId(null);
            }
        }
        response.setUrl(dto.getNotesUrl());
        response.setLocked(dto.isLocked());
        return response;
    }

    /**
     * Converts a {@link NoteDto} (from Room DB) to a {@link NotesResponse} for the sync request body.
     * String id is safely parsed to Integer; returns null id if parsing fails.
     */
    public static SyncDownloadNotesRequest fromNoteDtoToSyncDownloadNotesRequest(NoteDto dto) {
        SyncDownloadNotesRequest response = new SyncDownloadNotesRequest();
        if (dto.getId() != null) {
            try {
                response.setId(Integer.parseInt(dto.getId()));
            } catch (NumberFormatException e) {
                Log.w(TAG, "fromNoteDto: could not parse id='" + dto.getId() + "' as Integer");
                response.setId(null);
            }
        }
        response.setUrl(dto.getNotesUrl());
        response.setLocked(dto.isLocked());
        response.setType(dto.getNoteType());
        response.setSubjectName(dto.getSubjectName());
        return response;
    }

    /**
     * Converts a list of {@link NoteDto} to a list of {@link NotesResponse} for the sync request body.
     */
    public static List<NotesResponse> fromNoteDtoList(List<NoteDto> dtoList) {
        List<NotesResponse> result = new ArrayList<>();
        for (NoteDto dto : dtoList) {
            result.add(fromNoteDto(dto));
        }
        return result;
    }

    /**
     * Converts a list of {@link NoteDto} to a list of {@link SyncDownloadNotesRequest} for the sync request body.
     */
    public static List<SyncDownloadNotesRequest> fromNoteDtoListToSyncDownloadNotesRequestList(List<NoteDto> dtoList) {
        List<SyncDownloadNotesRequest> result = new ArrayList<>();
        for (NoteDto dto : dtoList) {
            result.add(fromNoteDtoToSyncDownloadNotesRequest(dto));
        }
        return result;
    }

    /**
     * Safely converts a {@link NotesResponse} integer id to a String suitable for Room DB lookup.
     * Returns null if the id is null.
     */
    public static String toEntityId(NotesResponse response) {
        return response.getId() != null ? String.valueOf(response.getId()) : null;
    }
}
