package arivihan.technologies.doubtbuzzter2.model.refer.uistate

import arivihan.technologies.doubtbuzzter2.model.refer.ReferAndEarnScreenData

data class ReferAndEarnScreenState(
    val loading: Boolean = false,
    val data: ReferAndEarnScreenData? = null,
    val error: String? = null
)
