package arivihan.technologies.doubtbuzzter2.toppernotes.presentation

import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperSubject

/**
 * Download state for a single note, mirrored out of the offline notes table.
 *
 * The states match what the legacy list adapter shows, so a note downloaded from either
 * screen reads the same in both.
 */
data class NoteDownloadState(
    val status: String? = null,
    val progress: Int = 0
) {
    val isComplete: Boolean
        get() = status == NoteEntity.STATUS_COMPLETED

    val isInProgress: Boolean
        get() = !isComplete && (
            progress > 0 ||
                status == NoteEntity.STATUS_PENDING ||
                status == NoteEntity.STATUS_IN_QUEUE ||
                status == NoteEntity.STATUS_DOWNLOADING
            )
}

data class TopperNotesUiState(
    val isLoadingToppers: Boolean = true,
    val isLoadingNotes: Boolean = false,
    val toppers: List<Topper> = emptyList(),
    val selectedTopperCode: String? = null,
    val selectedSubjectId: String? = null,
    val notes: List<TopperNote> = emptyList(),
    val errorMessage: String? = null,
    /**
     * Distinguishes a lost connection from a server error, so the screen can show the
     * offline state and retry itself once connectivity returns.
     */
    val isNetworkError: Boolean = false
) {
    val selectedTopper: Topper?
        get() = toppers.firstOrNull { it.code == selectedTopperCode }

    /** Chips come from whichever topper is selected. */
    val subjects: List<TopperSubject>
        get() = selectedTopper?.subjects.orEmpty()

    val isEmpty: Boolean
        get() = !isLoadingToppers && !isLoadingNotes && errorMessage == null && notes.isEmpty()

    /** True when the selected topper's accessType is "PRO" — triggers dark theme. */
    val isDarkTheme: Boolean
        get() = selectedTopper?.isPro == true
}
