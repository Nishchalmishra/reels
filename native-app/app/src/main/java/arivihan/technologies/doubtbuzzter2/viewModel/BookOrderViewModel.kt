package arivihan.technologies.doubtbuzzter2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.model.book_order.CartData
import arivihan.technologies.doubtbuzzter2.model.book_order.CartItemRequest
import arivihan.technologies.doubtbuzzter2.model.book_order.CatalogData
import arivihan.technologies.doubtbuzzter2.repository.BookOrderRepository
import arivihan.technologies.doubtbuzzter2.repository.OrderConfirmRepository

data class BookOrderUiState(
    val loading: Boolean = false,
    val data: CartData? = null,
    val error: String? = null
)

data class CatalogSheetState(
    val isVisible: Boolean = false,
    val loading: Boolean = false,
    val data: CatalogData? = null,
    val error: String? = null
)

data class BookOrderActionState(
    val loading: Boolean = false,
    val error: String? = null,
    val checkoutOrderId: String? = null
)




class BookOrderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val NETWORK_ERROR = "__network_error__"
    }

    private val repository = BookOrderRepository(application)
    private val orderConfirmRepository = OrderConfirmRepository(application)

    private val _cartState = MutableLiveData(BookOrderUiState(loading = true))
    val cartState: LiveData<BookOrderUiState> = _cartState

    private val _catalogSheetState = MutableLiveData(CatalogSheetState())
    val catalogSheetState: LiveData<CatalogSheetState> = _catalogSheetState

    private val _actionState = MutableLiveData(BookOrderActionState())
    val actionState: LiveData<BookOrderActionState> = _actionState

    init {
        refresh()
    }

    fun refresh() {
        if (!MyApplication.isNetworkAvailable()) {
            _cartState.postValue(BookOrderUiState(loading = false, error = NETWORK_ERROR))
            return
        }
        _cartState.value = BookOrderUiState(loading = true)
        repository.fetchCart(object : BookOrderRepository.CartCallback {
            override fun onSuccess(data: CartData) {
                _cartState.postValue(BookOrderUiState(loading = false, data = data))
            }
            override fun onError(message: String) {
                if (!MyApplication.isNetworkAvailable()) {
                    _cartState.postValue(BookOrderUiState(loading = false, error = NETWORK_ERROR))
                } else {
                    _cartState.postValue(BookOrderUiState(loading = false, error = message))
                }
            }
        })
    }




    //cart item CRUD

    fun addItem(
        item: CartItemRequest
    ) {
        _actionState.value = BookOrderActionState(loading = true)
        repository.addItem(item, object : BookOrderRepository.CartCallback {
            override fun onSuccess(data: CartData) {
                _actionState.postValue(BookOrderActionState(loading = false))
                _cartState.postValue(BookOrderUiState(loading = false, data = data))
            }
            override fun onError(message: String) {
                _actionState.postValue(BookOrderActionState(loading = false, error = message))
            }
        })
    }


    fun updateCart(
        items: List<CartItemRequest>
    ) {
        if (items.isEmpty()) return
        _actionState.value = BookOrderActionState(loading = true)
        repository.updateCartItems(items, object : BookOrderRepository.CartCallback {
            override fun onSuccess(data: CartData) {
                _actionState.postValue(BookOrderActionState(loading = false))
                _cartState.postValue(BookOrderUiState(loading = false, data = data))
            }
            override fun onError(message: String) {
                _actionState.postValue(BookOrderActionState(loading = false, error = message))
            }
        })
    }

    fun deleteItem(
        item: CartItemRequest
    ) {
        _actionState.value = BookOrderActionState(loading = true)
        repository.deleteItem(item, object : BookOrderRepository.CartCallback {
            override fun onSuccess(data: CartData) {
                _actionState.postValue(BookOrderActionState(loading = false))
                _cartState.postValue(BookOrderUiState(loading = false, data = data))
            }
            override fun onError(message: String) {
                _actionState.postValue(BookOrderActionState(loading = false, error = message))
            }
        })
    }

    fun openCatalogSheet() {
        _catalogSheetState.value = CatalogSheetState(isVisible = true, loading = true)
        repository.fetchCatalog(object : BookOrderRepository.CatalogCallback {
            override fun onSuccess(data: CatalogData) {
                _catalogSheetState.postValue(CatalogSheetState(isVisible = true, loading = false, data = data))
            }
            override fun onError(message: String) {
                _catalogSheetState.postValue(CatalogSheetState(isVisible = true, loading = false, error = message))
            }
        })
    }

    fun dismissCatalogSheet() {
        _catalogSheetState.value = CatalogSheetState()
    }

    fun initiateCheckout(addressId: String, freeSubjectCodes: List<String>) {
        _actionState.value = BookOrderActionState(loading = true)
        orderConfirmRepository.initiateCheckout(addressId, freeSubjectCodes, object : OrderConfirmRepository.CheckoutCallback {
            override fun onSuccess(data: OrderConfirmRepository.CheckoutData) {
                _actionState.postValue(BookOrderActionState(loading = false, checkoutOrderId = data.orderId))
            }
            override fun onError(message: String) {
                _actionState.postValue(BookOrderActionState(loading = false, error = message))
            }
        })
    }

    fun clearActionError() {
        _actionState.value = BookOrderActionState()
    }

    fun clearCheckoutOrderId() {
        _actionState.value = _actionState.value?.copy(checkoutOrderId = null) ?: BookOrderActionState()
    }
}
