package de.tolunla.ghostotp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.tolunla.ghostotp.otp.OneTimePassword.Crypto
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex

@Entity(tableName = "account")
@Suppress("DataClassPrivateConstructor")
data class Account private constructor(
    @PrimaryKey @ColumnInfo(name = "_id") val id: String,
    val label: String,
    val secret: String,
    val crypto: Crypto,
    val digits: Int,
    val type: Type,
    val issuer: String,
    val epoch: Long,
    val period: Int,
    val encoding: Encoding,
    var step: Long
) {
    enum class Type { TOTP, HOTP }
    enum class Encoding { BASE32, HEX }

    companion object {
        operator fun invoke(
            label: String, secret: String, crypto: Crypto = Crypto.SHA1, digits: Int = 6,
            type: Type, issuer: String = "", epoch: Long = 0L, period: Int = 30,
            encoding: Encoding = Encoding.BASE32, step: Long = 0L
        ): Account = Account(
            "$issuer$label", label, secret, crypto, digits,
            type, issuer, epoch, period, encoding, step
        )
    }

    fun getSecret(): ByteArray {
        return (if (encoding == Encoding.BASE32) Base32().decode(secret) else Hex.decodeHex(secret))
    }

    fun incrementStep() {
        if (type == Type.HOTP) step++
    }
}