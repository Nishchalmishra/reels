package arivihan.technologies.doubtbuzzter2.toppernotes.domain.usecase

import arivihan.technologies.doubtbuzzter2.network.ApiResult
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.repository.TopperNotesRepository
import javax.inject.Inject

class GetToppersUseCase @Inject constructor(
    private val repository: TopperNotesRepository
) {
    suspend operator fun invoke(): ApiResult<List<Topper>> = repository.getToppers()
}

class GetTopperNotesUseCase @Inject constructor(
    private val repository: TopperNotesRepository
) {
    suspend operator fun invoke(
        topperCode: String,
        subjectId: String
    ): ApiResult<List<TopperNote>> = repository.getNotes(topperCode, subjectId)
}
