package de.tolunla.ghostotp.model

import android.text.format.DateUtils
import de.tolunla.ghostotp.db.entity.AccountEntity.Type
import de.tolunla.steamguard.util.generateAuthCode
import org.json.JSONObject

/**
 * Class containing SteamGuard information
 */
class SteamAccount(
    id: Long? = null,
    name: String,
    issuer: String = "Steam",
    type: Type = Type.STEAM,
    private val sharedSecret: String,
    private val revocationCode: String,
    val identitySecret: String,
    val cookies: String = ""
) : Account(id, name, issuer, type) {

    override fun getProgress(): Float = System.currentTimeMillis()
        .rem(30 * DateUtils.SECOND_IN_MILLIS)
        .toFloat() / (30 * DateUtils.SECOND_IN_MILLIS)
        .toFloat()

    override fun generateCode(): String = generateAuthCode(sharedSecret, 0)

    override fun getJSON() = JSONObject(
        mapOf(
            "shared_secret" to sharedSecret,
            "revocation_code" to revocationCode,
            "identity_secret" to identitySecret,
            "cookies" to cookies,
        )
    ).toString()

    fun getRevocationCode() = revocationCode
}
