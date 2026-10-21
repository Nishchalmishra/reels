package arivihan.technologies.doubtbuzzter2.toppernotes.domain.repository

import arivihan.technologies.doubtbuzzter2.network.ApiResult
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote


interface TopperNotesRepository {

    suspend fun getToppers(): ApiResult<List<Topper>>

    suspend fun getNotes(topperCode: String, subjectId: String): ApiResult<List<TopperNote>>
}
