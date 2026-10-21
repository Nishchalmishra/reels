package arivihan.technologies.doubtbuzzter2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.model.order_history.OrderHistoryEntry
import arivihan.technologies.doubtbuzzter2.repository.OrderHistoryRepository

data class OrderHistoryUiState(
    val loading: Boolean = false,
    val orders: List<OrderHistoryEntry> = emptyList(),
    val error: String? = null
)

class OrderHistoryViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val NETWORK_ERROR = "__network_error__"
        private val EXCLUDED_STATUSES = setOf("EXPIRED", "PENDING", "CANCELLED")
        private const val FAILED_PAYMENT_STATUS = "FAILED"
        private val ALLOWED_PAYMENT_STATUSES = setOf("NONE", "PAID")
    }

    private val repository = OrderHistoryRepository(application)

    private val _uiState = MutableLiveData(OrderHistoryUiState(loading = true))
    val uiState: LiveData<OrderHistoryUiState> = _uiState

    fun loadOrders() {
        if (!MyApplication.isNetworkAvailable()) {
            _uiState.postValue(OrderHistoryUiState(loading = false, error = NETWORK_ERROR))
            return
        }
        _uiState.value = OrderHistoryUiState(loading = true)
        repository.fetchOrders(object : OrderHistoryRepository.Callback {
            override fun onSuccess(orders: List<OrderHistoryEntry>) {
                val filteredOrders = orders.filter { order ->
                    val statusAllowed = order.status.uppercase() !in EXCLUDED_STATUSES
                    val paymentNotFailed = order.paymentStatus.uppercase() != FAILED_PAYMENT_STATUS
                    val paymentAllowed = order.paymentStatus.uppercase() in ALLOWED_PAYMENT_STATUSES
                    statusAllowed && paymentNotFailed && paymentAllowed
                }
                _uiState.postValue(OrderHistoryUiState(loading = false, orders = filteredOrders))
            }

            override fun onError(message: String) {
                if (!MyApplication.isNetworkAvailable()) {
                    _uiState.postValue(OrderHistoryUiState(loading = false, error = NETWORK_ERROR))
                } else {
                    _uiState.postValue(OrderHistoryUiState(loading = false, error = message))
                }
            }
        })
    }
}
