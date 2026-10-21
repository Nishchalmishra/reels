package arivihan.technologies.doubtbuzzter2.network.interceptor

import android.content.Intent
import android.util.Log
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.activity.UserAuthenticationActivity
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clears the session and returns to login on a 401, matching what
 * [arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener] does for the
 * Volley stack. Living here keeps the side effect out of the call sites.
 */
@Singleton
class UnauthorizedInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var lastLogoutAt = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == HTTP_UNAUTHORIZED) {
            forceLogout()
        }
        return response
    }

    private fun forceLogout() {
        // parallel requests can all come back 401 at once
        val now = System.currentTimeMillis()
        if (now - lastLogoutAt < LOGOUT_THROTTLE_MS) return
        lastLogoutAt = now

        Log.d(TAG, "401 received, clearing session")
        val app = MyApplication.getInstance() ?: return
        MyApplication.clearSharedPref()

        val intent = Intent(app, UserAuthenticationActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        app.startActivity(intent)
    }

    private companion object {
        private const val TAG = "UnauthorizedInterceptor"
        private const val HTTP_UNAUTHORIZED = 401
        private const val LOGOUT_THROTTLE_MS = 5_000L
    }
}
