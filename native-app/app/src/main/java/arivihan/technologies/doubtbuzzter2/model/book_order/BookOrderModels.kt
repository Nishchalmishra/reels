package arivihan.technologies.doubtbuzzter2.model.book_order

import com.google.gson.annotations.SerializedName

data class CartApiResponse(
    @SerializedName("data") val data: CartData? = null,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class CartData(
    @SerializedName("items") val items: List<CartItem> = emptyList(),
    @SerializedName("itemCount") val itemCount: Int = 0,
    @SerializedName("freeCount") val freeCount: Int = 0,
    @SerializedName("paidCount") val paidCount: Int = 0,
    @SerializedName("summary") val summary: CartSummary? = null,
    @SerializedName("freeBooksAvailable") val freeBooksAvailable: Int = 0,
    @SerializedName("packageTiers") val packageTiers: List<PackageTier> = emptyList(),
    @SerializedName("recommendations") val recommendations: List<CartRecommendation> = emptyList(),
    @SerializedName("alsoAvailable") val alsoAvailable: List<AlsoAvailableBook> = emptyList(),
    @SerializedName("mrpTotal") val mrpTotal: Int = 0,
    @SerializedName("payable") val payable: Int = 0,
    @SerializedName("savings") val savings: Int = 0,
    @SerializedName("emptyTitle") val emptyTitle: String = "",
    @SerializedName("emptyImageUrl") val emptyImageUrl: String = "",
    @SerializedName("emptyButtonText") val emptyButtonText: String = "",
    @SerializedName("emptyShareText") val emptyShareText: String = ""
)

data class CartItem(
    @SerializedName("subjectCode") val subjectCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("mrp") val mrp: Int = 0,
    @SerializedName("unitPrice") val unitPrice: Int = 0,
    @SerializedName("qty") val qty: Int = 1,
    @SerializedName("lineTotal") val lineTotal: Int = 0,
    @SerializedName("discount") val discount: Int = 0,
    @SerializedName("discountPercent") val discountPercent: Int = 0,
    @SerializedName("free") val free: Boolean = false
)

data class CartSummary(
    @SerializedName("totalPrice") val totalPrice: Int = 0,
    @SerializedName("offerDiscount") val offerDiscount: Int = 0,
    @SerializedName("coinDiscount") val coinDiscount: Int = 0,
    @SerializedName("coinsUsed") val coinsUsed: Int = 0,
    @SerializedName("deliveryCharges") val deliveryCharges: Int = 0,
    @SerializedName("deliveryFree") val deliveryFree: Boolean = true,
    @SerializedName("finalTotal") val finalTotal: Int = 0
)

data class PackageTier(
    @SerializedName("bookCount") val bookCount: Int = 0,
    @SerializedName("price") val price: Int = 0
)

data class CartRecommendation(
    @SerializedName("bookCount") val bookCount: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("mrp") val mrp: Int = 0,
    @SerializedName("price") val price: Int = 0,
    @SerializedName("discount") val discount: Int = 0,
    @SerializedName("discountPercent") val discountPercent: Int = 0,
    @SerializedName("discountTag") val discountTag: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("bestDeal") val bestDeal: Boolean = false
)

data class AlsoAvailableBook(
    @SerializedName("subjectCode") val subjectCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("mrp") val mrp: Int = 0,
    @SerializedName("price") val price: Int = 0,
    @SerializedName("discount") val discount: Int = 0,
    @SerializedName("discountPercent") val discountPercent: Int = 0
)

data class CatalogApiResponse(
    @SerializedName("data") val data: CatalogData? = null,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class CatalogData(
    @SerializedName("books") val books: List<CatalogBook> = emptyList(),
    @SerializedName("packages") val packages: List<CatalogPackage> = emptyList()
)

data class CatalogBook(
    @SerializedName("id") val id: String = "",
    @SerializedName("subjectCode") val subjectCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("imageUrl") val imageUrl: String = "",
    @SerializedName("mrp") val mrp: Int = 0,
    @SerializedName("optional") val optional: Boolean = false
)

data class CatalogPackage(
    @SerializedName("bookCount") val bookCount: Int = 0,
    @SerializedName("price") val price: Int = 0,
    @SerializedName("perBookPrice") val perBookPrice: Int = 0,
    @SerializedName("discountTag") val discountTag: String = ""
)
