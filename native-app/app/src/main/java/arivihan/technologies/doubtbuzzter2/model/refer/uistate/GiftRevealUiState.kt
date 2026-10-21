package arivihan.technologies.doubtbuzzter2.model.refer.uistate

import arivihan.technologies.doubtbuzzter2.model.refer.GiftRevealData

data class GiftRevealUiState(
    val loading: Boolean = false,
    val data: GiftRevealData? = null,
    val error: String? = null
)
