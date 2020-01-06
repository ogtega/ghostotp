package de.tolunla.steamauth

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import java.util.*
import javax.crypto.Cipher
import kotlin.coroutines.resume

class SteamAuthLogin(private var username: String, private var password: String) {

  private val client = OkHttpClient()

  private val referer = "https://steamcommunity.com/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client"
  private var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"

  private val headers: Map<String, String> = mapOf(
    "X-Requested-With" to "com.valvesoftware.android.steam.community",
    "Referer" to referer,
    "User-Agent" to userAgent,
    "Accept" to "text/javascript, text/html, application/xml, text/xml, */*"
  )

  private var captchaGid: String = ""

  suspend fun doLogin(captcha: String = "", emailauth: String = "",
    twofactorcode: String = ""): String {

    val rsaObj = JSONObject(getRSAKey())

    Log.d("DoLogin", rsaObj.toString())

    val formBody = FormBody.Builder()
      .add("username", username)
      .add("password", encryptPassword(rsaObj))
      .add("captchagid", captchaGid)
      .add("captcha_text", captcha)
      .add("emailauth", emailauth)
      .add("twofactorcode", twofactorcode)
      .add("rsatimestamp", rsaObj.getString("timestamp"))
      .add("remember_login", "true")
      .add("loginfriendlyname", "#login_emailauth_friendlyname_mobile")
      .add("donotcache", Date().time.toString())
      .add("oauth_client_id", "DE45CD61")
      .add("oauth_scope", "read_profile write_profile read_client write_client")
      .build()

    val request = Request.Builder()
      .url("https://steamcommunity.com/login/dologin/")
      .post(formBody)
      .headers(headers.toHeaders())
      .build()

    return login(request)
  }

  private suspend fun login(request: Request): String =
    suspendCancellableCoroutine { continuation ->
      client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
          e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
          if (!response.isSuccessful) throw IOException("/dologin failed")

          response.body?.let {
            continuation.resume(it.string())
          }
        }
      })
    }

  private suspend fun getRSAKey(): String = suspendCancellableCoroutine { continuation ->
    val formBody = FormBody.Builder()
      .add("username", username)
      .build()

    val request = Request.Builder()
      .url("https://steamcommunity.com/login/getrsakey/")
      .post(formBody)
      .headers(headers.toHeaders())
      .build()

    client.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        e.printStackTrace()
      }

      override fun onResponse(call: Call, response: Response) {
        if (!response.isSuccessful) throw IOException("/getrsakey failed")

        response.body?.let {
          continuation.resume(it.string())
        }
      }
    })
  }

  private fun encryptPassword(rsaObj: JSONObject): String {
    val authMod = rsaObj.getString("publickey_mod")
    val authExp = rsaObj.getString("publickey_exp")
    val rsaParams = RSAPublicKeySpec(BigInteger(authMod, 16), BigInteger(authExp, 16))

    val factory = KeyFactory.getInstance("RSA")
    val pub = factory.generatePublic(rsaParams)
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    cipher.init(Cipher.ENCRYPT_MODE, pub)

    var passEncrypted = cipher.doFinal(password.toByteArray())
    passEncrypted = Base64.encode(passEncrypted, Base64.DEFAULT)

    return String(passEncrypted, Charsets.UTF_8)
  }
}
