package arivihan.technologies.doubtbuzzter2.model.refer.uistate

import arivihan.technologies.doubtbuzzter2.model.refer.AddressItem
import arivihan.technologies.doubtbuzzter2.model.refer.PincodeData

data class PincodeValidationState(
    val loading: Boolean = false,
    val data: PincodeData? = null,
    val error: String? = null
)

data class AddressListState(
    val loading: Boolean = false,
    val addresses: List<AddressItem> = emptyList(),
    val error: String? = null
)

data class CreateAddressState(
    val loading: Boolean = false,
    val address: AddressItem? = null,
    val error: String? = null
)