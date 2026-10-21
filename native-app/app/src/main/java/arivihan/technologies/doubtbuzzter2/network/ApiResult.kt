package arivihan.technologies.doubtbuzzter2.network

import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import androidx.annotation.StringRes
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed interface ApiResult<out T> {

    data class Success<out T>(val data: T) : ApiResult<T>

    data class HttpError(val statusCode: Int, val message: String) : ApiResult<Nothing>

    data class NetworkError(val message: String, val cause: Throwable) : ApiResult<Nothing>
}

val ApiResult<*>.errorMessage: String?
    get() = when (this) {
        is ApiResult.Success -> null
        is ApiResult.HttpError -> message
        is ApiResult.NetworkError -> message
    }

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        ApiResult.HttpError(e.code(), e.serverMessage() ?: string(R.string.slow_internet_error))
    } catch (e: IOException) {
        ApiResult.NetworkError(
            string(
                if (MyApplication.isNetworkAvailable()) R.string.slow_internet_error
                else R.string.internet_failure_error
            ),
            e
        )
    }

private val errorGson = Gson()

private fun HttpException.serverMessage(): String? = try {
    response()?.errorBody()?.string()
        ?.let { errorGson.fromJson(it, ErrorBody::class.java) }
        ?.message
        ?.takeIf { it.isNotBlank() }
} catch (e: Exception) {
    null
}

private fun string(@StringRes resId: Int): String =
    MyApplication.getInstance().getString(resId)

private data class ErrorBody(
    val status: Int?,
    val message: String?,
    val details: String?
)
