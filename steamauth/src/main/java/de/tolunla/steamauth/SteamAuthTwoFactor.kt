package de.tolunla.steamauth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.math.floor

class SteamAuthTwoFactor(private val loginResult: SteamAuthLogin.LoginResult) {

  private val client = SteamAuthUtils.getClient()
  private val headers = SteamAuthUtils.getHeaders()

  suspend fun enableTwoFactor() = withContext(Dispatchers.IO) {

    val formBody = FormBody.Builder()
      .add("steamid", loginResult.steamID)
      .add("access_token", loginResult.oathToken)
      .add("authenticator_time", floor(Date().time.div(1000.0)).toString())
      .add("authenticator_type", "1")
      .add("device_identifier", SteamAuthUtils.getDeviceId(loginResult.steamID))
      .add("sms_phone_id", "1")
      .build()

    val request = Request.Builder()
      .url("https://api.steampowered.com/ITwoFactorService/AddAuthenticator/v1/")
      .headers(headers)
      .post(formBody)
      .build()

    client.newCall(request).execute().use { res ->
      if (!res.isSuccessful) throw IOException("/AddAuthenticator failed")
      val data = JSONObject(res.body?.string() ?: "")

      Log.d("AddAuthenticator", data.toString())
    }
  }

  suspend fun finalizeTwoFactor() = withContext(Dispatchers.IO) {
  }

  suspend fun disableTwoFactor() = withContext(Dispatchers.IO) {
  }
}
