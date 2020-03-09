package de.tolunla.steamauth

import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import org.apache.commons.codec.digest.DigestUtils

class SteamAuthUtils {
    companion object {

        fun getHeaders(): Headers {
            val referer =
                "https://steamcommunity.com/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client"
            val userAgent =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"

            val headers: Map<String, String> = mapOf(
                "X-Requested-With" to "com.valvesoftware.android.steam.community",
                "Referer" to referer,
                "User-Agent" to userAgent,
                "Accept" to "text/javascript, text/html, application/xml, text/xml, */*"
            )

            return headers.toHeaders()
        }

        fun getClient(): OkHttpClient {
            return OkHttpClient.Builder().cookieJar(
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
    }
}