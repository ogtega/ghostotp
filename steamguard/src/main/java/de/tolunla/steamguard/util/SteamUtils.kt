package de.tolunla.steamguard.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
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
