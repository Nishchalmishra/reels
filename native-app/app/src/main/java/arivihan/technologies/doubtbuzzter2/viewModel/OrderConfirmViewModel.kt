package arivihan.technologies.doubtbuzzter2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arivihan.technologies.doubtbuzzter2.model.order_success.OrderView
import arivihan.technologies.doubtbuzzter2.repository.OrderConfirmRepository

data class OrderConfirmUiState(
    val loading: Boolean = false,
    val data: OrderView? = null,
    val error: String? = null
)

data class CheckoutState(
    val loading: Boolean = false,
    val data: OrderConfirmRepository.CheckoutData? = null,
    val error: String? = null
)

class OrderConfirmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OrderConfirmRepository(application)

    private val _uiState = MutableLiveData(OrderConfirmUiState(loading = true))
    val uiState: LiveData<OrderConfirmUiState> = _uiState

    private val _checkoutState = MutableLiveData(CheckoutState())
    val checkoutState: LiveData<CheckoutState> = _checkoutState

    fun initiateCheckout(addressId: String?, subjectCodes: List<String>) {
        _checkoutState.value = CheckoutState(loading = true)
        repository.initiateCheckout(addressId, subjectCodes,
            object : OrderConfirmRepository.CheckoutCallback {
                override fun onSuccess(data: OrderConfirmRepository.CheckoutData) {
                    _checkoutState.postValue(CheckoutState(data = data))
                }

                override fun onError(message: String) {
                    _checkoutState.postValue(CheckoutState(error = message))
                }
            })
    }

    fun confirmOrder(orderId: String, paymentId: String, signature: String) {
        _uiState.value = OrderConfirmUiState(loading = true)
        repository.confirmOrder(orderId, paymentId, signature,
            object : OrderConfirmRepository.Callback {
                override fun onSuccess(order: OrderView) {
                    _uiState.postValue(OrderConfirmUiState(loading = false, data = order))
                }

                override fun onError(message: String) {
                    _uiState.postValue(OrderConfirmUiState(loading = false, error = message))
                }
            })
    }

    fun redeemOrder(subjectCodes: List<String>, addressId: String?) {
        _uiState.value = OrderConfirmUiState(loading = true)
        repository.redeemOrder(subjectCodes, addressId,
            object : OrderConfirmRepository.Callback {
                override fun onSuccess(order: OrderView) {
                    _uiState.postValue(OrderConfirmUiState(loading = false, data = order))
                }

                override fun onError(message: String) {
                    _uiState.postValue(OrderConfirmUiState(loading = false, error = message))
                }
            })
    }

    fun getOrderById(orderId: String) {
        _uiState.value = OrderConfirmUiState(loading = true)
        repository.getOrderById(orderId,
            object : OrderConfirmRepository.Callback {
                override fun onSuccess(order: OrderView) {
                    _uiState.postValue(OrderConfirmUiState(loading = false, data = order))
                }

                override fun onError(message: String) {
                    _uiState.postValue(OrderConfirmUiState(loading = false, error = message))
                }
            })
    }
}
