package arivihan.technologies.doubtbuzzter2.toppernotes.data.mapper

import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto.TopperDto
import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto.TopperNoteDto
import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto.TopperSubjectDto
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperSubject

/** Drops inactive and unusable entries, and applies the server's displayOrder. */
fun List<TopperDto>?.toToppers(): List<Topper> =
    orEmpty()
        .filter { it.isActive != false && !it.code.isNullOrBlank() }
        .sortedBy { it.displayOrder ?: Int.MAX_VALUE }
        .map { it.toTopper() }

private fun TopperDto.toTopper() = Topper(
    code = code.orEmpty(),
    name = name.orEmpty(),
    examYear = examYear ?: 0,
    rank = rank ?: 0,
    selectedImageUrl = selectedImageUrl.orEmpty(),
    unselectedImageUrl = unselectedImageUrl.orEmpty(),
    subjects = subjects.toSubjects(),
    accessType = accessType.orEmpty()
)

private fun List<TopperSubjectDto>?.toSubjects(): List<TopperSubject> =
    orEmpty()
        .filter { !it.id.isNullOrBlank() }
        .map { TopperSubject(id = it.id.orEmpty(), name = it.name.orEmpty()) }

fun List<TopperNoteDto>?.toNotes(): List<TopperNote> =
    orEmpty()
        .filter { it.isActive != false }
        .sortedBy { it.displayOrder ?: Int.MAX_VALUE }
        .map {
            TopperNote(
                id = it.id ?: 0,
                code = it.code.orEmpty(),
                subjectName = it.subjectName.orEmpty(),
                title = it.title.orEmpty(),
                notesBy = it.notesBy.orEmpty(),
                url = it.url.orEmpty(),
                type = it.type.orEmpty(),
                accessType = it.accessType.orEmpty(),
                locked = it.locked ?: false
            )
        }
