package arivihan.technologies.doubtbuzzter2.toppernotes.domain.model

import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType

data class Topper(
    val code: String,
    val name: String,
    val examYear: Int,
    val rank: Int,
    val selectedImageUrl: String,
    val unselectedImageUrl: String,
    val subjects: List<TopperSubject>,
    val accessType: String
) {
    val isPro: Boolean get() = accessType.equals(SubscriptionType.PRO.name, ignoreCase = true)
}

data class TopperSubject(
    val id: String,
    val name: String
)

data class TopperNote(
    val id: Int,
    val code: String,
    val subjectName: String,
    val title: String,
    val notesBy: String,
    val url: String,
    val type: String,
    val accessType: String,
    val locked: Boolean
)
