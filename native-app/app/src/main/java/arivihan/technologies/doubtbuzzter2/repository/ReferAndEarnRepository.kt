package arivihan.technologies.doubtbuzzter2.repository

import android.app.Application
import android.util.Log
import com.android.volley.Request
import com.google.gson.Gson
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes
import arivihan.technologies.doubtbuzzter2.helper.EnvironmentManager
import arivihan.technologies.doubtbuzzter2.model.refer.*
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton
import com.google.gson.reflect.TypeToken
import org.json.JSONObject


class ReferAndEarnRepository(private val application: Application) {

    private val gson = Gson()

    companion object {
        private const val TAG = "ReferAndEarnRepository"
    }


    //screen
    fun getReferAndEarnScreen(
        callback: (success: Boolean, data: ReferAndEarnScreenData?, error: String?) -> Unit
    ) {
        val request = MyJsonObjectRequest(
            Request.Method.GET,
            UrlRoutes.REFER_EARN_SCREEN,
            null,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<ReferAndEarnScreenData>>() {}.type
                    val screenResponse: ApiResponseDto<ReferAndEarnScreenData> = gson.fromJson(response.toString(), type)
                    if (screenResponse.success && screenResponse.data != null) {
                        callback(true, screenResponse.data, null)
                    } else {
                        val errorMessage = screenResponse.message ?: "Failed to load screen data"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }






    fun validatePincode(
        pincode: String,
        callback: (success: Boolean, data: PincodeData?, error: String?) -> Unit
    ) {
        val request = MyJsonObjectRequest(
            Request.Method.GET,
            "${UrlRoutes.REFER_EARN_PINCODE_VALIDATION}/$pincode",
            null,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<PincodeData>>() {}.type
                    val pincodeResponse: ApiResponseDto<PincodeData> = gson.fromJson(response.toString(), type)
                    if (pincodeResponse.success && pincodeResponse.data != null) {
                        callback(true, pincodeResponse.data, null)
                    } else {
                        val errorMessage = pincodeResponse.message ?: "Invalid Pincode"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }




    fun getAddressList(
        callback: (success: Boolean, addresses: List<AddressItem>?, error: String?) -> Unit
    ) {
        val request = MyJsonObjectRequest(
            Request.Method.GET,
            UrlRoutes.REFER_EARN_ADDRESS_LIST,
            null,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<List<AddressItem>>>() {}.type
                    val addressResponse: ApiResponseDto<List<AddressItem>> = gson.fromJson(response.toString(), type)
                    if (addressResponse.success && addressResponse.data != null) {
                        callback(true, addressResponse.data, null)
                    } else {
                        val errorMessage = addressResponse.message ?: "No addresses found"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }




    fun getLeaderboard(
        scope: String = "global",
        page: Int = 0,
        size: Int = 10,
        callback: (success: Boolean, data: LeaderboardData?, error: String?) -> Unit
    ) {
        val request = MyJsonObjectRequest(
            Request.Method.GET,
            "${UrlRoutes.REFER_EARN_LEADERBOARD}?scope=$scope&page=$page&size=$size",
            null,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<LeaderboardData>>() {}.type
                    val leaderboardResponse: ApiResponseDto<LeaderboardData> = gson.fromJson(response.toString(), type)
                    if (leaderboardResponse.success && leaderboardResponse.data != null) {
                        callback(true, leaderboardResponse.data, null)
                    } else {
                        val errorMessage = leaderboardResponse.message ?: "Failed to load leaderboard"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }





    fun getGiftReveal(
        callback: (success: Boolean, data: GiftRevealData?, error: String?) -> Unit
    ) {
        val request = MyJsonObjectRequest(
            Request.Method.GET,
            UrlRoutes.REFER_EARN_GIFT_REVEAL,
            null,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<GiftRevealData>>() {}.type
                    val giftResponse: ApiResponseDto<GiftRevealData> = gson.fromJson(response.toString(), type)
                    if (giftResponse.success && giftResponse.data != null) {
                        callback(true, giftResponse.data, null)
                    } else {
                        val errorMessage = giftResponse.message ?: "Failed to load gift reveal"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gift reveal parse error", e)
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                Log.e(TAG, "Gift reveal API error", error)
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }


    fun getInvitedFriendsReferralList(
        page: Int = 0,
        size: Int = 10,
        callback: (success: Boolean, data: FriendsReferralData?, error: String?) -> Unit
    ) {
        val request = MyJsonObjectRequest(
            Request.Method.GET,
            "${UrlRoutes.REFER_EARN_FRIENDS}?page=$page&size=$size",
            null,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<FriendsReferralData>>() {}.type
                    val friendsResponse: ApiResponseDto<FriendsReferralData> = gson.fromJson(response.toString(), type)
                    if (friendsResponse.success && friendsResponse.data != null) {
                        callback(true, friendsResponse.data, null)
                    } else {
                        val errorMessage = friendsResponse.message ?: "No invited friends found"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Invited friends parse error", e)
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                Log.e(TAG, "Invited friends API error", error)
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }


    fun createAddress(
        addressRequest: CreateAddressRequest,
        callback: (success: Boolean, data: AddressItem?, error: String?) -> Unit
    ) {
        val jsonBody = JSONObject(gson.toJson(addressRequest))

        val request = MyJsonObjectRequest(
            Request.Method.POST,
            UrlRoutes.REFER_EARN_ADDRESS_LIST,
            jsonBody,
            { response ->
                try {
                    val type = object : TypeToken<ApiResponseDto<AddressItem>>() {}.type
                    val addressResponse: ApiResponseDto<AddressItem> = gson.fromJson(response.toString(), type)
                    if (addressResponse.success && addressResponse.data != null) {
                        callback(true, addressResponse.data, null)
                    } else {
                        val errorMessage = addressResponse.message ?: "Failed to create address"
                        callback(false, null, errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Create address parse error", e)
                    callback(false, null, e.message ?: "Parse error")
                }
            },
            { error ->
                Log.e(TAG, "Create address API error", error)
                callback(false, null, error.message ?: "Network error")
            }
        )

        MyVolleySingleton.getInstance(application).addToRequestQueue(request)
    }

}
