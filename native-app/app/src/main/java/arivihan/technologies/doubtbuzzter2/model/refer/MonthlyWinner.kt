package arivihan.technologies.doubtbuzzter2.model.refer

data class MonthlyWinner(
    val id: String,
    val rank: Int,
    val name: String,
    val coins: Int,
    val referralCount: Int,
    val isCurrentUser: Boolean = false,
    val illustration: String? = null,
    val avatarUrl: String? = null
)

data class MonthlyWinnersResponse(
    val winners: List<MonthlyWinner>,
    val currentUserRank: MonthlyWinner? = null,
    val hasMore: Boolean,
    val lastId: String?
)
