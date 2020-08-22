package de.tolunla.steamguard.util

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.codec.digest.DigestUtils

/**
 * Gets an instance of a okHTTP client configured for steam
 * @return OkHttpClient with steam cookies
 */
fun getClient(): OkHttpClient {
    return OkHttpClient.Builder().addInterceptor(SteamInterceptor()).cookieJar(
        object : CookieJar {

            private val cookieStore = mutableListOf(
                Cookie.Builder().domain("steamcommunity.com").name("mobileClientVersion")
                    .value("0 (2.1.3)").build(),
                Cookie.Builder().domain("steamcommunity.com").name("mobileClient")
                    .value("android").build()
            )

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore
            }

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore.addAll(cookies)
            }
        }
    ).build()
}

fun getWebViewClient(): WebViewClient {
    val client = getClient()

    return object : WebViewClient() {
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            return request?.let {
                request.requestHeaders.remove("X-Requested-With")
                request.requestHeaders.remove("Referer")
                request.requestHeaders.remove("User-Agent")

                val req =
                    Request.Builder().url(it.url.toString()).headers(it.requestHeaders.toHeaders())
                        .build()

                client.newCall(req).execute().use { res ->
                    res.body?.use { body ->
                        WebResourceResponse(
                            res.header("content-type", "text/plain"),
                            res.header("content-encoding", "utf-8"),
                            body.string().byteInputStream()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generates a deviceID for steam using a steamID
 * @param steamID a steamID64 string
 * @return a standardized deviceID for the current user
 */
fun getDeviceId(steamID: String): String {
    val raw = DigestUtils.sha1Hex(steamID)
    return "android:${raw.substring(0..7)}" +
        "-${raw.substring(8..11)}" +
        "-${raw.substring(12..15)}" +
        "-${raw.substring(16..19)}" +
        "-${raw.substring(20..31)}"
}

/**
 * Data class for the state of a steam login
 */
data class SteamLoginResult(
    val success: Boolean = false,
    val emailCode: Boolean = false,
    val emailDomain: String = "",
    val require2fa: Boolean = false,
    val captcha: Boolean = false,
    val captchaGid: String = "",
    val oathToken: String = "",
    val steamID: String = ""
)
