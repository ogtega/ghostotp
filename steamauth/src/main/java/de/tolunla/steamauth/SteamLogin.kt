package de.tolunla.steamauth

import de.tolunla.steamauth.util.SteamLoginResult
import de.tolunla.steamauth.util.*
import okhttp3.FormBody
import okhttp3.Request
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

class SteamLogin(private val username: String, private val password: String) {

    private val client = getClient()
    private var captchaGid: String = ""

    fun doLogin(captcha: String = "", emailAuth: String = "", mobileCode: String = ""): SteamLoginResult {
        val rsaObj = JSONObject(getRSAKey())

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", encryptPassword(rsaObj))
            .add("captchagid", captchaGid)
            .add("captcha_text", captcha)
            .add("emailauth", emailAuth)
            .add("twofactorcode", mobileCode)
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

            return SteamLoginResult(
                success = data.optBoolean("success", false),
                emailCode = data.optBoolean("emailauth_needed", false),
                mobileCode = data.optBoolean("requires_twofactor", false),
                captcha = data.optBoolean("captcha_needed", false),
                captchaGid = data.optString("captcha_gid", "-1"),
                oathToken = oath.optString("oauth_token", ""),
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