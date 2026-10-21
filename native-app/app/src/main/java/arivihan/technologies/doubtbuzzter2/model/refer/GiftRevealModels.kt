package arivihan.technologies.doubtbuzzter2.model.refer

import com.google.gson.annotations.SerializedName

data class GiftRevealData(
    @SerializedName("unlockedCount")
    val unlockedCount: Int = 0,

    @SerializedName("booksAlreadyOrdered")
    val booksAlreadyOrdered: Int = 0,

    @SerializedName("totalBooks")
    val totalBooks: Int = 0,

    @SerializedName("maxSelectable")
    val maxSelectable: Int = 1,

    @SerializedName("revealAssetUrl")
    val revealAssetUrl: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("subtitle")
    val subtitle: String? = null,

    @SerializedName("cardInfoTitle")
    val cardInfoTitle: String? = null,

    @SerializedName("cardInfoSubTitle")
    val cardInfoSubTitle: String? = null,

    @SerializedName("selectableBooks")
    val selectableBooks: List<SelectableBook> = emptyList(),

    @SerializedName("books")
    val books: List<BookItem> = emptyList()
)

data class SelectableBook(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("subjectCode")
    val subjectCode: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("imageUrl")
    val imageUrl: String = "",

    @SerializedName("mrp")
    val mrp: Int = 0,

    @SerializedName("optional")
    val optional: Boolean = false
)

data class BookItem(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("subjectCode")
    val subjectCode: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("imageUrl")
    val imageUrl: String = "",

    @SerializedName("mrp")
    val mrp: Int = 0,

    @SerializedName("optional")
    val optional: Boolean = false,

    @SerializedName("selectable")
    val selectable: Boolean = false,

    @SerializedName("inCart")
    val inCart: Boolean = false,

    @SerializedName("redeemed")
    val redeemed: Boolean = false,

    @SerializedName("delivered")
    val delivered: Boolean = false
)
