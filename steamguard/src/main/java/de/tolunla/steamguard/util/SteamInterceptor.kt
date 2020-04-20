package de.tolunla.steamguard.util

import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class contains the information all steam api requests need.
 */
class SteamInterceptor : Interceptor {

    private val referer =
        "https://steamcommunity.com/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client"
    private val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"

    private val headers: Map<String, String> = mapOf(
        "X-Requested-With" to "com.valvesoftware.android.steam.community",
        "Referer" to referer,
        "User-Agent" to userAgent,
        "Accept" to "text/javascript, text/html, application/xml, text/xml, */*"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .headers(headers.toHeaders())
            .build()

        return chain.proceed(request)
    }
}
