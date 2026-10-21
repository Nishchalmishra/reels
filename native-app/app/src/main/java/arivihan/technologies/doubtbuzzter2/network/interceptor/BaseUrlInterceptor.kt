package arivihan.technologies.doubtbuzzter2.network.interceptor

import arivihan.technologies.doubtbuzzter2.BuildConfig
import arivihan.technologies.doubtbuzzter2.MyApplication
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Re-points every request at whatever `MyApplication.getEndPoint()` currently returns.
 *
 * Retrofit resolves its `baseUrl` once, when the `@Singleton` instance is built, so switching
 * environment from Settings would leave it talking to the server chosen at process start until
 * the app was killed. The Volley stack has no such problem — it concatenates `getEndPoint()`
 * per request — so the two stacks would silently disagree mid-session.
 *
 * Retrofit is therefore built against [BuildConfig.BASE_URL], a fixed value, and this rewrites
 * the scheme, host, port and base path on the way out. Anchoring on the build-time constant
 * (rather than on whatever the endpoint happened to be at startup) is what makes the prefix
 * swap deterministic.
 */
@Singleton
class BaseUrlInterceptor @Inject constructor() : Interceptor {

    /** The base Retrofit was built with, and therefore the prefix on every outgoing path. */
    private val buildTimeBase = BuildConfig.BASE_URL.toHttpUrl()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val active = MyApplication.getEndPoint()
            ?.takeIf { it.isNotBlank() }
            ?.toHttpUrlOrNull()
            ?: return chain.proceed(request)

        if (active.isSameBaseAs(buildTimeBase)) {
            return chain.proceed(request)
        }

        // Swap the build-time prefix for the active one, keeping the endpoint-specific tail.
        // e.g. /arivihan-platform/secure/api/v1/x  ->  /other-prefix/secure/api/v1/x
        val tail = request.url.encodedPath.removePrefix(buildTimeBase.trimmedPath())
        val rebased = active.newBuilder()
            .encodedPath(active.trimmedPath() + tail)
            .encodedQuery(request.url.encodedQuery)
            .fragment(request.url.fragment)
            .build()

        return chain.proceed(request.newBuilder().url(rebased).build())
    }

    private fun okhttp3.HttpUrl.trimmedPath(): String = encodedPath.trimEnd('/')

    private fun okhttp3.HttpUrl.isSameBaseAs(other: okhttp3.HttpUrl): Boolean =
        scheme == other.scheme &&
            host == other.host &&
            port == other.port &&
            trimmedPath() == other.trimmedPath()
}
