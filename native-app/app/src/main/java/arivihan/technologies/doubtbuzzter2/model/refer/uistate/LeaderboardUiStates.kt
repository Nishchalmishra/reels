package arivihan.technologies.doubtbuzzter2.model.refer.uistate

import arivihan.technologies.doubtbuzzter2.model.refer.LeaderboardData
import arivihan.technologies.doubtbuzzter2.model.refer.MonthlyWinner

data class LeaderboardState(
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val data: LeaderboardData? = null,
    val error: String? = null
)

data class MonthlyWinnersUiState(
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val selfRank: MonthlyWinner? = null,
    val winners: List<MonthlyWinner> = emptyList(),
    val currentUserRank: MonthlyWinner? = null,
    val hasMore: Boolean = false,
    val lastId: String? = null,
    val error: String? = null,
)
