package de.tolunla.steamauth

import android.util.Base64
import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

class SteamAuthLogin(private val username: String, private val password: String) {

  private var client = OkHttpClient()

  private val referer = "https://steamcommunity.com/mobilelogin?oauth_client_id=DE45CD61&oauth_scope=read_profile%20write_profile%20read_client%20write_client"
  private var user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"

  private val headers: Map<String, String> = mapOf(
    "X-Requested-With" to "com.valvesoftware.android.steam.community",
    "Referer" to referer,
    "User-Agent" to user_agent,
    "Accept" to "text/javascript, text/html, application/xml, text/xml, */*"
  )

  fun getRSAKey(): String {
    val formBody = FormBody.Builder()
      .add("username", username)
      .build()

    val request = Request.Builder()
      .url("https://steamcommunity.com/login/getrsakey/")
      .post(formBody)
      .headers(headers.toHeaders())
      .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) throw IOException("/getrsakey failed")

      response.body?.let {
        return it.string()
      }
    }

    throw IOException("/getrsakey failed")
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
