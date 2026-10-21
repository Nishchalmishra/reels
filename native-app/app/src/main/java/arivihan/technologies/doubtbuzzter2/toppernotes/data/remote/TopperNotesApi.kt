package arivihan.technologies.doubtbuzzter2.toppernotes.data.remote

import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto.ApiEnvelope
import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto.TopperNoteDto
import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto.ToppersDataDto
import arivihan.technologies.doubtbuzzter2.utils.ApiConstants.BATCH_ID
import arivihan.technologies.doubtbuzzter2.utils.ApiConstants.GET_TOPPERS
import arivihan.technologies.doubtbuzzter2.utils.ApiConstants.GET_TOPPER_NOTES
import arivihan.technologies.doubtbuzzter2.utils.ApiConstants.SUBJECT_ID
import arivihan.technologies.doubtbuzzter2.utils.ApiConstants.TOPPER_CODE
import retrofit2.http.GET
import retrofit2.http.Query

/** Paths are relative — the base URL already carries the `/arivihan-platform/` prefix. */
interface TopperNotesApi {

    @GET(GET_TOPPERS)
    suspend fun getToppers(
        @Query(BATCH_ID) batchId: Int
    ): ApiEnvelope<ToppersDataDto>

    @GET(GET_TOPPER_NOTES)
    suspend fun getNotes(
        @Query(BATCH_ID) batchId: Int,
        @Query(TOPPER_CODE) topperCode: String,
        @Query(SUBJECT_ID) subjectId: String
    ): ApiEnvelope<List<TopperNoteDto>>
}
