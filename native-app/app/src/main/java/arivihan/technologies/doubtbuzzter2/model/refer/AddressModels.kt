package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

typealias PincodeResponse = ApiResponseDto<PincodeData>
typealias AddressListResponse = ApiResponseDto<List<AddressItem>>
typealias CreateAddressResponse = ApiResponseDto<AddressItem>

data class CreateAddressRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("line1") val line1: String,
    @SerializedName("line2") val line2: String?,
    @SerializedName("landmark") val landmark: String?,
    @SerializedName("pincode") val pincode: String,
    @SerializedName("city") val city: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("makeDefault") val makeDefault: Boolean = true
)

data class PincodeData(
    @SerializedName("pincode") val pincode: String,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("serviceable") val serviceable: Boolean
)

data class AddressItem(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("line1") val line1: String,
    @SerializedName("line2") val line2: String?,
    @SerializedName("landmark") val landmark: String?,
    @SerializedName("pincode") val pincode: String,
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("defaultAddress") val defaultAddress: Boolean
)
