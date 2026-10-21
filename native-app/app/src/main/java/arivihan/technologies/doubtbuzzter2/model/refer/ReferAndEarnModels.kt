package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

data class ReferAndEarnSummaryResponse(
    @SerializedName("referral_code")
    val referralCode: String = "",
    @SerializedName("points")
    val points: Int = 0,
    @SerializedName("invite_count")
    val inviteCount: Int = 0,
    @SerializedName("headline")
    val headline: String = "",
    @SerializedName("subheadline")
    val subheadline: String = "",
    @SerializedName("share_message")
    val shareMessage: String = "",
    @SerializedName("invite_link")
    val inviteLink: String = "",
    @SerializedName("terms")
    val terms: List<String> = emptyList(),
    @SerializedName("rules")
    val rules: List<String> = emptyList(),
    @SerializedName("history_preview")
    val historyPreview: List<ReferAndEarnHistoryItem> = emptyList()
)

data class ReferAndEarnTopBannerResponse(
    @SerializedName("ui_version")
    val uiVersion: String = "v3",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("subtitle")
    val subtitle: String = "",
    @SerializedName("cta_text")
    val ctaText: String = "",
    @SerializedName("badge_text")
    val badgeText: String = "",
    @SerializedName("highlight_value")
    val highlightValue: String = "",
    @SerializedName("highlight_label")
    val highlightLabel: String = "",
    @SerializedName("secondary_label")
    val secondaryLabel: String = "",
    @SerializedName("secondary_value")
    val secondaryValue: String = "",
    @SerializedName("accent_note")
    val accentNote: String = "",
    @SerializedName("icon_name")
    val iconName: String = ""
)

data class ReferAndEarnHistoryItem(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("subtitle")
    val subtitle: String = "",
    @SerializedName("date")
    val date: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("points")
    val points: Int = 0
)
