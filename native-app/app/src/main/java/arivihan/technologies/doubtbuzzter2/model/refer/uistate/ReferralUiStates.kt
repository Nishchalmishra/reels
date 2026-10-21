package arivihan.technologies.doubtbuzzter2.model.refer.uistate

import arivihan.technologies.doubtbuzzter2.model.refer.InvitedFriendReferralItem


data class FriendsReferralUiState(
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val friendsReferrals: List<InvitedFriendReferralItem> = emptyList(),
    val currentPage: Int = 0,
    val hasMore: Boolean = false,
    val error: String? = null
)
