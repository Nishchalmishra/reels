package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

data class ApiResponseDto<T>(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("code")
    val code: Int = 0,

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: T? = null
)
