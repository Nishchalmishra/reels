package arivihan.technologies.doubtbuzzter2.toppernotes.data.repository

import arivihan.technologies.doubtbuzzter2.network.ApiResult
import arivihan.technologies.doubtbuzzter2.network.safeApiCall
import arivihan.technologies.doubtbuzzter2.toppernotes.data.local.BatchLocalSource
import arivihan.technologies.doubtbuzzter2.toppernotes.data.mapper.toNotes
import arivihan.technologies.doubtbuzzter2.toppernotes.data.mapper.toToppers
import arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.TopperNotesApi
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.repository.TopperNotesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopperNotesRepositoryImpl @Inject constructor(
    private val api: TopperNotesApi,
    private val batchLocalSource: BatchLocalSource
) : TopperNotesRepository {

    override suspend fun getToppers(): ApiResult<List<Topper>> = safeApiCall {
        api.getToppers(batchLocalSource.batchId()).data?.toppers.toToppers()
    }

    override suspend fun getNotes(
        topperCode: String,
        subjectId: String
    ): ApiResult<List<TopperNote>> = safeApiCall {
        api.getNotes(
            batchId = batchLocalSource.batchId(),
            topperCode = topperCode,
            subjectId = subjectId
        ).data.toNotes()
    }
}
