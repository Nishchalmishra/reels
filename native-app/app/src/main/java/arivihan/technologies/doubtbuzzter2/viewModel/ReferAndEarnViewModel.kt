package arivihan.technologies.doubtbuzzter2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.compose.ArivihanScreenState
import arivihan.technologies.doubtbuzzter2.model.refer.*
import arivihan.technologies.doubtbuzzter2.model.refer.uistate.*
import arivihan.technologies.doubtbuzzter2.repository.ReferAndEarnRepository








class ReferAndEarnViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val PAGE_SIZE = 10

    private val repository = ReferAndEarnRepository(application)





    //refer and earn
    private val _screenState = MutableLiveData<ArivihanScreenState<ReferAndEarnScreenData>>(ArivihanScreenState.Loading)
    val screenState: LiveData<ArivihanScreenState<ReferAndEarnScreenData>> = _screenState

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    // Called by RDB listener — if coin mismatch, refresh API
    fun onRdbCoinUpdate(coins: Int) {
        val currentData = (_screenState.value as? ArivihanScreenState.Content)?.data ?: return
        val currentCoins = currentData.coinsCard?.totalCoins ?: return
        if (coins != currentCoins) {
            refreshReferAndEarnScreen()
        }
    }




    //address
    private val _pincodeState = MutableLiveData(PincodeValidationState())
    val pincodeState: LiveData<PincodeValidationState> = _pincodeState

    private val _addressListState = MutableLiveData(AddressListState())
    val addressListState: LiveData<AddressListState> = _addressListState

    private val _createAddressState = MutableLiveData(CreateAddressState())
    val createAddressState: LiveData<CreateAddressState> = _createAddressState





    //leaderboard
    private val _leaderboardState = MutableLiveData(LeaderboardState())
    val leaderboardState: LiveData<LeaderboardState> = _leaderboardState



    //monthly winners
    private val _monthlyWinnersState = MutableLiveData(MonthlyWinnersUiState())
    val monthlyWinnersState: LiveData<MonthlyWinnersUiState> = _monthlyWinnersState



    //gift reveal / book selection
    private val _giftRevealState = MutableLiveData(GiftRevealUiState())
    val giftRevealState: LiveData<GiftRevealUiState> = _giftRevealState

    //referral history
    private val _friendsReferralUiState = MutableLiveData(FriendsReferralUiState())
    val friendsReferralUiState: LiveData<FriendsReferralUiState> = _friendsReferralUiState







    //load refer and screen
    fun loadReferAndEarnScreen() {
        _screenState.value = ArivihanScreenState.Loading
        fetchReferAndEarnScreen()
    }


    //refresh
    fun refreshReferAndEarnScreen() {
        if (_screenState.value is ArivihanScreenState.Loading) return
        _isRefreshing.value = true
        if (_screenState.value is ArivihanScreenState.Content) {
            fetchReferAndEarnScreen()
        } else {
            _screenState.value = ArivihanScreenState.Loading
            fetchReferAndEarnScreen()
        }
    }

    private fun fetchReferAndEarnScreen() {
        if (!MyApplication.isNetworkAvailable()) {
            _screenState.postValue(ArivihanScreenState.NetworkError)
            return
        }

        repository.getReferAndEarnScreen { success, data, error ->
            _isRefreshing.postValue(false)
            if (success && data != null) {
                _screenState.postValue(ArivihanScreenState.Content(data))
            } else {
                if (_screenState.value is ArivihanScreenState.Content) return@getReferAndEarnScreen
                val errorState = if (!MyApplication.isNetworkAvailable()) {
                    ArivihanScreenState.NetworkError
                } else {
                    ArivihanScreenState.Error(error)
                }
                _screenState.postValue(errorState)
            }
        }
    }









    // Address Management
    fun validatePincode(pincode: String) {
        _pincodeState.value = PincodeValidationState(loading = true)

        repository.validatePincode(pincode) { success, data, error ->
            if (success && data != null) {
                _pincodeState.postValue(PincodeValidationState(loading = false, data = data))
            } else {
                _pincodeState.postValue(PincodeValidationState(loading = false, error = error))
            }
        }
    }

    fun loadAddressList() {
        _addressListState.value = AddressListState(loading = true)

        repository.getAddressList { success, addresses, error ->
            if (success && addresses != null) {
                _addressListState.postValue(AddressListState(loading = false, addresses = addresses))
            } else {
                _addressListState.postValue(AddressListState(loading = false, error = error))
            }
        }
    }

    fun createAddress(request: CreateAddressRequest) {
        _createAddressState.value = CreateAddressState(loading = true)

        repository.createAddress(request) { success, data, error ->
            if (success && data != null) {
                _createAddressState.postValue(CreateAddressState(loading = false, address = data))
            } else {
                _createAddressState.postValue(CreateAddressState(loading = false, error = error))
            }
        }
    }

    // Leaderboard with pagination
    fun loadLeaderboard(scope: String = "global", refresh: Boolean = false) {
        val currentState = _leaderboardState.value

        if (refresh || currentState?.data == null) {
            _leaderboardState.value = LeaderboardState(loading = true)

            repository.getLeaderboard(scope, 0, PAGE_SIZE) { success, data, error ->
                if (success && data != null) {
                    _leaderboardState.postValue(LeaderboardState(loading = false, data = data))
                } else {
                    _leaderboardState.postValue(LeaderboardState(loading = false, error = error))
                }
            }
        }
    }

    fun loadMoreLeaderboard(scope: String = "global") {
        val currentState = _leaderboardState.value ?: return
        val currentData = currentState.data ?: return

        if (currentState.loadingMore || !currentData.hasNext) return

        _leaderboardState.value = currentState.copy(loadingMore = true)

        val nextPage = currentData.page + 1

        repository.getLeaderboard(scope, nextPage, PAGE_SIZE) { success, data, error ->
            if (success && data != null) {
                val updatedWinners = currentData.winners + data.winners
                val updatedData = data.copy(winners = updatedWinners, page = nextPage)
                _leaderboardState.postValue(LeaderboardState(loading = false, loadingMore = false, data = updatedData))
            } else {
                _leaderboardState.postValue(currentState.copy(loadingMore = false, error = error))
            }
        }
    }



    fun loadGiftReveal() {
        _giftRevealState.value = GiftRevealUiState(loading = true)

        repository.getGiftReveal { success, data, error ->
            if (success && data != null) {
                _giftRevealState.postValue(GiftRevealUiState(loading = false, data = data))
            } else {
                _giftRevealState.postValue(GiftRevealUiState(loading = false, error = error))
            }
        }
    }


    fun loadReferralHistoryPaginated() {
        _friendsReferralUiState.value = FriendsReferralUiState(loading = true)

        repository.getInvitedFriendsReferralList(page = 0, size = PAGE_SIZE) { success, data, error ->
            if (success && data != null) {
                _friendsReferralUiState.postValue(
                    FriendsReferralUiState(
                        loading = false,
                        friendsReferrals = data.friends,
                        currentPage = data.page,
                        hasMore = data.hasNext
                    )
                )
            } else {
                _friendsReferralUiState.postValue(FriendsReferralUiState(loading = false, error = error))
            }
        }
    }


    fun loadMoreReferralHistory() {
        val currentState = _friendsReferralUiState.value ?: return
        if (currentState.loadingMore || !currentState.hasMore) return

        _friendsReferralUiState.value = currentState.copy(loadingMore = true)

        val nextPage = currentState.currentPage + 1

        repository.getInvitedFriendsReferralList(page = nextPage, size = PAGE_SIZE) { success, data, error ->
            if (success && data != null) {
                val updatedFriends = currentState.friendsReferrals + data.friends
                _friendsReferralUiState.postValue(
                    FriendsReferralUiState(
                        loading = false,
                        loadingMore = false,
                        friendsReferrals = updatedFriends,
                        currentPage = data.page,
                        hasMore = data.hasNext
                    )
                )
            } else {
                _friendsReferralUiState.postValue(currentState.copy(loadingMore = false, error = error))
            }
        }
    }



}
