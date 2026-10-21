package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

typealias InvitedFriendsListResponse = ApiResponseDto<FriendsReferralData>

data class FriendsReferralData(
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrevious") val hasPrevious: Boolean,
    @SerializedName("friends") val friends: List<InvitedFriendReferralItem>
)

data class InvitedFriendReferralItem(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("maskedNumber") val maskedNumber: String,
    @SerializedName("illustration") val illustration: String,
    @SerializedName("illustrationIndex") val illustrationIndex: Int,
    @SerializedName("status") val status: String,
    @SerializedName("statusText") val statusText: String,
    @SerializedName("windowExpiresAt") val windowExpiresAt: String?,
    @SerializedName("steps") val steps: List<InviteStep>?,
    @SerializedName("states") val states: List<InviteState>?,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

data class InviteStep(
    @SerializedName("label") val label: String,
    @SerializedName("done") val done: Boolean,
    @SerializedName("failed") val failed: Boolean
)

data class InviteState(
    @SerializedName("state") val state: String,
    @SerializedName("label") val label: String,
    @SerializedName("status") val status: String
)