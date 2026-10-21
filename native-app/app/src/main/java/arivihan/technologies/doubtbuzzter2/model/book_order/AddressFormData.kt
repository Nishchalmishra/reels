package arivihan.technologies.doubtbuzzter2.model.book_order

data class AddressFormData(
    val name: String = "",
    val contactNumber: String = "",
    val pinCode: String = "",
    val houseNo: String = "",
    val locality: String = "",
    val city: String = "",
    val state: String = ""
)
