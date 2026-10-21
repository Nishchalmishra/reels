package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

data class CoinMilestone(

    @SerializedName("point")
    val point: Int,

    @SerializedName("rewardType")
    val rewardType: String,

    @SerializedName("milestone_icon")
    val milestoneIcon: Int?,

    @SerializedName("milestone_banner")
    val milestoneBanner: String?,

    @SerializedName("is_unlocked")
    val isUnlocked: Boolean = false
)
