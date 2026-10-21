package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName


typealias ReferAndEarnScreenResponse = ApiResponseDto<ReferAndEarnScreenData>

data class ReferAndEarnScreenData(
    @SerializedName("flowType") val flowType: String = "",
    @SerializedName("stage") val stage: String = "",
    @SerializedName("entryPoint") val entryPoint: EntryPoint? = null,
    @SerializedName("bannerImageUrl") val bannerImageUrl: String? = null,
    @SerializedName("poster") val poster: Poster? = null,
    @SerializedName("coinsCard") val coinsCard: CoinsCard? = null,
    @SerializedName("progress") val progress: Progress? = null,
    @SerializedName("cta") val cta: CtaData? = null,
    @SerializedName("buttons") val buttons: List<ActionButton?>? = null,
    @SerializedName("invitedFriends") val invitedFriends: InvitedFriends? = null,
    @SerializedName("monthlyWinners") val monthlyWinners: MonthlyWinnersData? = null,
    @SerializedName("infoSection") val infoSection: InfoSection? = null,
    @SerializedName("video") val video: Video? = null,
    @SerializedName("callbackCard") val callbackCard: CallbackCard? = null,
    @SerializedName("share") val share: ShareData? = null,
    @SerializedName("giftRevealText") val giftRevealText: GiftRevealText? = null,
    @SerializedName("cartBadge") val cartBadge: Int? = null
)

data class GiftRevealText(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = ""
)

data class EntryPoint(
    @SerializedName("label") val label: String = "",
    @SerializedName("iconUrl") val iconUrl: String = "",
    @SerializedName("allFeaturesPosition") val allFeaturesPosition: Int = 0,
    @SerializedName("mode") val mode: String = ""
)

data class Poster(
    @SerializedName("stage") val stage: String = "",
    @SerializedName("imageUrl") val imageUrl: String = ""
)

data class CoinsCard(
    @SerializedName("title") val title: String = "",
    @SerializedName("totalCoins") val totalCoins: Int = 0,
    @SerializedName("coinsPerReferral") val coinsPerReferral: Int = 0
)

data class Progress(
    @SerializedName("milestones") val milestones: List<Milestone> = emptyList(),
    @SerializedName("nextMilestoneCoins") val nextMilestoneCoins: Int? = null,
    @SerializedName("coinsToNextMilestone") val coinsToNextMilestone: Int? = null,
    @SerializedName("animate") val animate: Boolean = false
)

data class Milestone(
    @SerializedName("coins") val coins: Int = 0,
    @SerializedName("rewardType") val rewardType: String = "",
    @SerializedName("achieved") val achieved: Boolean = false,
    @SerializedName("index") val index: Int = 0,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("coinImageUrl") val coinImageUrl: String? = null
)

data class CtaData(
    @SerializedName("state") val state: String = "",
    @SerializedName("primary") val primary: ButtonAction? = null,
    @SerializedName("secondary") val secondary: ButtonAction? = null
)

data class ButtonAction(
    @SerializedName("label") val label: String = "",
    @SerializedName("action") val action: String = ""
)

data class ActionButton(
    @SerializedName("id") val id: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("style") val style: String? = null,
    @SerializedName("action") val action: ButtonActionData? = null,
    @SerializedName("enabled") val enabled: Boolean = true
)

data class ButtonActionData(
    @SerializedName("type") val type: String? = null,
    @SerializedName("screenId") val screenId: String? = null,
    @SerializedName("activity") val activity: String? = null,
    @SerializedName("params") val params: String? = null
)

data class InfoSection(
    @SerializedName("title") val title: String = "",
    @SerializedName("tips") val tips: List<String> = emptyList()
)

data class Video(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("thumbnailUrl") val thumbnailUrl: String = ""
)

data class CallbackCard(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subTitle: String = "",
    @SerializedName("phone") val phone: String = ""
)

data class ShareData(
    @SerializedName("text") val text: String = "",
    @SerializedName("link") val link: String = ""
)

data class MonthlyWinnersData(
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("winners") val winners: List<LeaderboardWinner> = emptyList()
)

data class InvitedFriends(
    @SerializedName("friends") val friends: List<InvitedFriendReferralItem> = emptyList(),
    @SerializedName("hasMore") val hasMore: Boolean = false,
    @SerializedName("totalCount") val totalCount: Int = 0
)
