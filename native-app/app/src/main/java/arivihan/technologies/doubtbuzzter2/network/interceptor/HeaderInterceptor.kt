package arivihan.technologies.doubtbuzzter2.network.interceptor

import android.util.Log
import arivihan.technologies.doubtbuzzter2.volley.VolleyRequestHelper
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeaderInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        val headers: Map<String?, String?>? = VolleyRequestHelper.getHeaders()
        for ((name, value) in headers.orEmpty()) {
            if (name == null || value == null || original.header(name) != null) {
                continue
            }
            try {
                builder.header(name, value)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Skipping invalid header: $name", e)
            }
        }

        return chain.proceed(builder.build())
    }

    private companion object {
        private const val TAG = "HeaderInterceptor"
    }
}
