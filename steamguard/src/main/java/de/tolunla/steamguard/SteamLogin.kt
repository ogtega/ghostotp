package de.tolunla.steamguard

import de.tolunla.steamguard.util.SteamLoginResult
import de.tolunla.steamguard.util.getClient
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

/**
 * Class responsible for making all login related api requests.
 * @param username the username of the account logging in.
 * @param password the password of the account logging in.
 */
class SteamLogin(private val username: String, private val password: String) {

    private val client = getClient()

    /**
     * Sends the login api request with provided arguments
     * @param captcha the captcha string for the login request
     * @param emailAuth the code sent to the account's email
     * @return the result of the login request.
     */
    fun doLogin(
        captcha: String = "",
        emailAuth: String = "",
        prevResult: SteamLoginResult = SteamLoginResult()
    ): SteamLoginResult {
        val rsaObj = JSONObject(getRSAKey())

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", encryptPassword(rsaObj))
            .add("captchagid", prevResult.captchaGid)
            .add("captcha_text", captcha)
            .add("emailauth", emailAuth)
            .add("twofactorcode", "")
            .add("rsatimestamp", rsaObj.getString("timestamp"))
            .add("remember_login", "true")
            .add("loginfriendlyname", "#login_emailauth_friendlyname_mobile")
            .add("donotcache", System.currentTimeMillis().toString())
            .add("oauth_client_id", "DE45CD61")
            .add("oauth_scope", "read_profile write_profile read_client write_client")
            .build()

        val request = Request.Builder()
            .url("https://steamcommunity.com/login/dologin/")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) throw IOException("/dologin failed")
            val data = JSONObject(res.body?.string() ?: "")
            val oath = JSONObject(data.optString("oauth", "{}"))
            val cookieMap = mutableMapOf<Any?, Any?>()

            val cookies = client.cookieJar.loadForRequest("https://steamcommunity.com".toHttpUrl())
                .toMutableList()

            cookies.addAll(client.cookieJar.loadForRequest("http://steamcommunity.com".toHttpUrl()))

            for (cookie in cookies) {
                if (cookie.name in setOf(
                        "steamLogin",
                        "steamLoginSecure",
                        "steamMachineAuth",
                        "sessionid"
                    )
                ) {
                    cookieMap[cookie.name] = cookie.value
                }
            }

            return SteamLoginResult(
                success = data.optBoolean("success", false),
                emailCode = data.optBoolean("emailauth_needed", false),
                emailDomain = data.optString("emaildomain", ""),
                require2fa = data.optBoolean("requires_twofactor", false),
                captcha = data.optBoolean("captcha_needed", false),
                captchaGid = data.optString("captcha_gid", prevResult.captchaGid),
                oathToken = oath.optString("oauth_token", ""),
                cookies = JSONObject(cookieMap).toString(),
                steamID = oath.optString("steamid", "")
            )
        }
    }

    private fun getRSAKey(): String {
        val formBody = FormBody.Builder()
            .add("username", username)
            .build()

        val request = Request.Builder()
            .url("https://steamcommunity.com/login/getrsakey/")
            .post(formBody)
            .build()

        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) throw IOException("/getrsakey failed")
            return res.body?.string() ?: ""
        }
    }

    private fun encryptPassword(rsaObj: JSONObject): String {
        val authMod = rsaObj.getString("publickey_mod")
        val authExp = rsaObj.getString("publickey_exp")

        val rsaParams = RSAPublicKeySpec(
            BigInteger(authMod, 16),
            BigInteger(authExp, 16)
        )

        val factory = KeyFactory.getInstance("RSA")
        val pub = factory.generatePublic(rsaParams)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

        cipher.init(Cipher.ENCRYPT_MODE, pub)

        var passEncrypted = cipher.doFinal(StringUtils.getBytesUtf8(password))
        passEncrypted = Base64.encodeBase64(passEncrypted)

        return StringUtils.newStringUtf8(passEncrypted)
    }
}
