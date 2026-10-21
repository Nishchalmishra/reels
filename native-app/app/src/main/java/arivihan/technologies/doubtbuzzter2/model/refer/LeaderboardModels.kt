package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

typealias LeaderboardResponse = ApiResponseDto<LeaderboardData>

data class LeaderboardData(
    @SerializedName("scope") val scope: String,
    @SerializedName("scopeLabel") val scopeLabel: String,
    @SerializedName("districtAvailable") val districtAvailable: Boolean,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("hasPrevious") val hasPrevious: Boolean,
    @SerializedName("winners") val winners: List<LeaderboardWinner>,
    @SerializedName("self") val self: LeaderboardWinner
)

data class LeaderboardWinner(
    @SerializedName("rank") val rank: Int,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("stream") val stream: String,
    @SerializedName("referrals") val referrals: Int,
    @SerializedName("coins") val coins: Int,
    @SerializedName("you") val you: Boolean,
    @SerializedName("illustration") val illustration: String,
    @SerializedName("illustrationIndex") val illustrationIndex: Int,
    @SerializedName("avatarUrl") val avatarUrl: String?
)
