package arivihan.technologies.doubtbuzzter2.model.order_history

import com.google.gson.annotations.SerializedName
import arivihan.technologies.doubtbuzzter2.model.order_success.AddressView

data class OrderHistoryResponse(
    @SerializedName("data") val data: List<OrderHistoryEntry> = emptyList(),
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class OrderHistoryEntry(
    @SerializedName("id") val id: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("paymentStatus") val paymentStatus: String = "",
    @SerializedName("totalAmount") val totalAmount: Int = 0,
    @SerializedName("bookCount") val bookCount: Int = 0,
    @SerializedName("placedAt") val placedAt: String = "",
    @SerializedName("trackingId") val trackingId: String? = null,
    @SerializedName("address") val address: AddressView? = null,
    @SerializedName("items") val items: List<HistoryBookItem> = emptyList()
)

data class HistoryBookItem(
    @SerializedName("subjectCode") val subjectCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("qty") val qty: Int = 1,
    @SerializedName("mrp") val mrp: Int = 0,
    @SerializedName("unitPrice") val unitPrice: Int = 0,
    @SerializedName("lineTotal") val lineTotal: Int = 0,
    @SerializedName("discount") val discount: Int = 0,
    @SerializedName("discountPercent") val discountPercent: Int = 0,
    @SerializedName("coinUsage") val coinUsage: Int = 0,
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("free") val free: Boolean = false,
)
