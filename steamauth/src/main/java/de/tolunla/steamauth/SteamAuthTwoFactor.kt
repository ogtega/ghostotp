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

    /**
     * response.status:
     * 02 - Phone number not attached to account
     * 84 - RateLimitExceeded
     *
     * {
    "shared_secret":"mKsMqOhllo7YzT0EV\/U4FZMuI9E=",
    "serial_number":"7278042377951074840",
    "revocation_code":"R94313",
    "uri":"otpauth:\/\/totp\/Steam:ghostotp?secret=TCVQZKHIMWLI5WGNHUCFP5JYCWJS4I6R&issuer=Steam",
    "server_time":"1578936914",
    "account_name":"ghostotp",
    "token_gid":"1681c3eccbb52a40",
    "identity_secret":"ocWNbfJHTh9LrAZYkv2SP\/5N8aU=",
    "secret_1":"GU6RXfU1zrljZZ7uVxnhLqsucFg=",
    "status":1
    }
     */

    client.newCall(request).execute().use { res ->
      if (!res.isSuccessful) throw IOException("/AddAuthenticator failed")

      val data = JSONObject(
        JSONObject(res.body?.string() ?: "")
          .optString("response", "{}")
      )

      Log.d("AddAuthenticator", data.toString())
    }
  }

  suspend fun finalizeTwoFactor() = withContext(Dispatchers.IO) {
  }

  suspend fun disableTwoFactor() = withContext(Dispatchers.IO) {
  }
}
