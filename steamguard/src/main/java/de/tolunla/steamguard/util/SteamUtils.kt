package de.tolunla.steamguard.util

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.apache.commons.codec.digest.DigestUtils

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

fun getDeviceId(steamID: String): String {
    val raw = DigestUtils.sha1Hex(steamID)
    return "android:${raw.substring(0..7)}" +
            "-${raw.substring(8..11)}" +
            "-${raw.substring(12..15)}" +
            "-${raw.substring(16..19)}" +
            "-${raw.substring(20..31)}"
}

data class SteamGuardResult(
    val serverTime: Int,
    val sharedSecret: String,
    val identitySecret: String,
    val secretOne: String,
    val status: Int,
    val success: Boolean = status == 1
)

data class SteamLoginResult(
    val success: Boolean,
    val emailCode: Boolean,
    val mobileCode: Boolean,
    val captcha: Boolean,
    private val captchaGid: String,
    val captchaURL: String = "https://steamcommunity.com/login/rendercaptcha/?gid=${captchaGid}",
    val oathToken: String,
    val steamID: String
)
