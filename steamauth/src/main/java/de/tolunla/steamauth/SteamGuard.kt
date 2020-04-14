package de.tolunla.steamauth

import android.util.Log
import de.tolunla.steamauth.util.*
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.math.floor

/**
 * Class responsible for making all login related api requests.
 *
 * @property steamID the id of the account where enabling SteamGuard.
 * @property token steam Oauth token.
 * @constructor Creates an steamguard object with a user's credentials.
 */
class SteamGuard(private val steamID: String, private val token: String) {
    private val client = getClient()

    /**
     * Sends the api request to enable SteamGuard
     * @return the result of the request.
     */
    fun enableTwoFactor(): SteamGuardResult {
        val formBody = FormBody.Builder()
            .add("steamid", steamID)
            .add("access_token", token)
            .add("authenticator_time", floor(Date().time.div(1000.0)).toString())
            .add("authenticator_type", "1")
            .add("device_identifier", getDeviceId(steamID))
            .add("sms_phone_id", "1")
            .build()

        val request = Request.Builder()
            .url("https://api.steampowered.com/ITwoFactorService/AddAuthenticator/v1/")
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

            val json = JSONObject(
                JSONObject(res.body?.string() ?: "")
                    .optString("response", "{}")
            )

            Log.d("2fa", json.toString())

            return SteamGuardResult(
                serverTime = json.optInt("server_time", 0),
                sharedSecret = json.optString("shared_secret", ""),
                identitySecret = json.optString("identity_secret", ""),
                secretOne = json.optString("secret_1", ""),
                status = json.optInt("status", 0)
            )
        }
    }

    /**
     * Sends the api request to finalize SteamGuard
     */
    fun finalizeTwoFactor(secret: String, smsCode: String) {
        val code = SteamGuardUtils.generateAuthCode(secret, 0)
        val time = System.currentTimeMillis().div(1000).toInt()

        val formBody = FormBody.Builder()
            .add("steamid", steamID)
            .add("access_token", token)
            .add("authenticator_code", code)
            .add("authenticator_time", time.toString())
            .add("activation_code", smsCode)
            .build()

        val request = Request.Builder()
            .url("https://api.steampowered.com/ITwoFactorService/FinalizeAddAuthenticator/v1/")
            .post(formBody)
            .build()

        client.newCall(request).execute().use {res ->
            if (!res.isSuccessful) throw IOException("/FinalizeAddAuthenticator failed")

            val json = JSONObject(
                JSONObject(res.body?.string() ?: "")
                    .optString("response", "{}")
            )

            Log.d("2fa", json.toString())

            // TODO: if (json.getInt("status") == 89) Invalid activation code
            // TODO: if (json.getBoolean("want_more"))

            // TODO: if (!json.getBoolean("success"))
        }
    }
}
