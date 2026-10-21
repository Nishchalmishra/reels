package arivihan.technologies.doubtbuzzter2.model.book_order

data class CartItemRequest(
    val subjectCode: String,
    val qty: Int,
    val free: Boolean = false
)
