package arivihan.technologies.doubtbuzzter2.network.di

import arivihan.technologies.doubtbuzzter2.BuildConfig
import arivihan.technologies.doubtbuzzter2.network.interceptor.BaseUrlInterceptor
import arivihan.technologies.doubtbuzzter2.network.interceptor.HeaderInterceptor
import arivihan.technologies.doubtbuzzter2.network.interceptor.UnauthorizedInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        baseUrlInterceptor: BaseUrlInterceptor,
        headerInterceptor: HeaderInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            // First, so the logging interceptor below reports the URL actually requested.
            .addInterceptor { chain ->
                if (BuildConfig.REELS_DEMO && chain.request().url.host != "pbaegeqjargtsfortsyh.supabase.co") {
                    okhttp3.Response.Builder().request(chain.request()).protocol(okhttp3.Protocol.HTTP_1_1)
                        .code(503).message("Unavailable in isolated demo")
                        .body(okhttp3.ResponseBody.create(null as okhttp3.MediaType?, "No production request was sent.")).build()
                } else chain.proceed(chain.request())
            }
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(headerInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .apply {
                if (BuildConfig.DEBUG && !BuildConfig.REELS_DEMO) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("token")
                            redactHeader("access-key")
                        }
                    )
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    /**
     * Deliberately the build-time constant, not `MyApplication.getEndPoint()`. Retrofit
     * resolves this once for the lifetime of the `@Singleton`, so reading the live endpoint
     * here would bake in whichever environment happened to be selected at process start.
     * [BaseUrlInterceptor] re-points each request instead, matching how Volley behaves.
     */
    private fun baseUrl(): String =
        BuildConfig.BASE_URL.let { if (it.endsWith("/")) it else "$it/" }
}
