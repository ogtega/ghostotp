package de.tolunla.steamguard

import android.util.Log
import de.tolunla.steamguard.util.SteamGuardResult
import de.tolunla.steamguard.util.SteamGuardUtils
import de.tolunla.steamguard.util.getClient
import de.tolunla.steamguard.util.getDeviceId
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
    fun finalizeTwoFactor(secret: String, smsCode: String): Boolean {
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

        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) throw IOException("/FinalizeAddAuthenticator failed")

            val json = JSONObject(
                JSONObject(res.body?.string() ?: "")
                    .optString("response", "{}")
            )

            return json.getBoolean("success")
        }
    }
}
