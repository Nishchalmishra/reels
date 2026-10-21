package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

data class InvitedFriendUiModel(
    val displayName: String,
    val maskedNumber: String,
    val illustration: String,
    val illustrationIndex: Int,
    val status: InviteStatus,
    val statusText: String,
    val windowExpiresAt: String?,
    val steps: List<InviteStep> = emptyList(),
    val states: List<InviteState> = emptyList(),
    val showRewardBadge: Boolean = false,
    val description: String = "",
    val avatarUrl: String? = null
)

enum class InviteStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("successful")
    SUCCESSFUL,

    @SerializedName("expired")
    EXPIRED
}
