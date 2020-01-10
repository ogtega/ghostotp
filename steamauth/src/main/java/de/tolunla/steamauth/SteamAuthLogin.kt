package de.tolunla.steamauth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.StringUtils
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher


class SteamAuthLogin(private var username: String, private var password: String) {

  private val client = OkHttpClient.Builder().cookieJar(
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

  private val referer = "https://steamcommunity.com/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client"
  private var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"

  private val headers: Map<String, String> = mapOf(
    "X-Requested-With" to "com.valvesoftware.android.steam.community",
    "Referer" to referer,
    "User-Agent" to userAgent,
    "Accept" to "text/javascript, text/html, application/xml, text/xml, */*"
  )

  suspend fun doLogin(captcha: String = "", captchaGid: String = "", emailAuth: String = "",
    twoFactorCode: String = ""): LoginResult = withContext(Dispatchers.IO) {

    val rsaObj = JSONObject(getRSAKey())

    Log.d("DoLogin", rsaObj.toString())

    val formBody = FormBody.Builder()
      .add("username", username)
      .add("password", encryptPassword(rsaObj))
      .add("captchagid", captchaGid)
      .add("captcha_text", captcha)
      .add("emailauth", emailAuth)
      .add("twofactorcode", twoFactorCode)
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
      .headers(headers.toHeaders())
      .build()

    client.newCall(request).execute().use { res ->
      if (!res.isSuccessful) throw IOException("/dologin failed")
      val data = JSONObject(res.body?.string() ?: "")
      val oath = JSONObject(data.optString("oauth", "{}"))

      Log.d("DoLogin", data.toString())

      return@withContext LoginResult(
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

  private suspend fun getRSAKey(): String = withContext(Dispatchers.IO) {
    val formBody = FormBody.Builder()
      .add("username", username)
      .build()

    val request = Request.Builder()
      .url("https://steamcommunity.com/login/getrsakey/")
      .post(formBody)
      .headers(headers.toHeaders())
      .build()

    client.newCall(request).execute().use { res ->
      if (!res.isSuccessful) throw IOException("/getrsakey failed")
      return@withContext res.body?.string() ?: ""
    }
  }

  private fun encryptPassword(rsaObj: JSONObject): String {
    val authMod = rsaObj.getString("publickey_mod")
    val authExp = rsaObj.getString("publickey_exp")
    val rsaParams = RSAPublicKeySpec(BigInteger(authMod, 16), BigInteger(authExp, 16))

    val factory = KeyFactory.getInstance("RSA")
    val pub = factory.generatePublic(rsaParams)
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    cipher.init(Cipher.ENCRYPT_MODE, pub)

    var passEncrypted = cipher.doFinal(StringUtils.getBytesUtf8(password))
    passEncrypted = Base64.encodeBase64(passEncrypted)

    return StringUtils.newStringUtf8(passEncrypted)
  }

  data class LoginResult(
    val success: Boolean,
    val emailCode: Boolean,
    val mobileCode: Boolean,
    val captcha: Boolean,
    val captchaGid: String,
    val captchaURL: String = "https://steamcommunity.com/login/rendercaptcha/?gid=${captchaGid}",
    val oathToken: String,
    val steamID: String
  )
}
