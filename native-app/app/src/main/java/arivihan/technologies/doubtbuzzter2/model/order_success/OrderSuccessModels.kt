package arivihan.technologies.doubtbuzzter2.model.order_success

import com.google.gson.annotations.SerializedName

data class OrderConfirmResponse(
    @SerializedName("data") val data: OrderView? = null,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class OrderView(
    @SerializedName("id") val id: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("paymentStatus") val paymentStatus: String = "",
    @SerializedName("totalAmount") val totalAmount: Int = 0,
    @SerializedName("bookCount") val bookCount: Int = 0,
    @SerializedName("placedAt") val placedAt: String = "",
    @SerializedName("address") val address: AddressView? = null,
    @SerializedName("items") val items: List<OrderItemView> = emptyList()
)

data class OrderItemView(
    @SerializedName("subjectCode") val subjectCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("qty") val qty: Int = 1,
    @SerializedName("unitPrice") val unitPrice: Int = 0,
    @SerializedName("coinUsage") val coinUsage: Int = 0,
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("free") val free: Boolean = false
)

data class AddressView(
    @SerializedName("id") val id: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("line1") val line1: String = "",
    @SerializedName("line2") val line2: String = "",
    @SerializedName("landmark") val landmark: String = "",
    @SerializedName("pincode") val pincode: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("state") val state: String = "",
    @SerializedName("defaultAddress") val defaultAddress: Boolean = false
)
