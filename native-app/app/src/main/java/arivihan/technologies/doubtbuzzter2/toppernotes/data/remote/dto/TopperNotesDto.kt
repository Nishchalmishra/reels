package arivihan.technologies.doubtbuzzter2.toppernotes.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Envelope every topper-notes endpoint wraps its payload in. */
data class ApiEnvelope<T>(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

data class ToppersDataDto(
    @SerializedName("toppers") val toppers: List<TopperDto>?
)

data class TopperDto(
    @SerializedName("code") val code: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("examYear") val examYear: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("selectedImageUrl") val selectedImageUrl: String?,
    @SerializedName("unselectedImageUrl") val unselectedImageUrl: String?,
    @SerializedName("subjects") val subjects: List<TopperSubjectDto>?,
    @SerializedName("accessType") val accessType: String?,
    @SerializedName("displayOrder") val displayOrder: Int?,
    @SerializedName("isActive") val isActive: Boolean?
)

data class TopperSubjectDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
)

data class TopperNoteDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("code") val code: String?,
    @SerializedName("subjectName") val subjectName: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("notesBy") val notesBy: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("accessType") val accessType: String?,
    @SerializedName("displayOrder") val displayOrder: Int?,
    @SerializedName("locked") val locked: Boolean?,
    @SerializedName("isActive") val isActive: Boolean?
)
