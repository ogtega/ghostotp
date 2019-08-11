package de.tolunla.ghostotp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.tolunla.ghostotp.otp.OneTimePassword.Crypto
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex

@Entity(tableName = "account")
data class Account(
    @PrimaryKey val label: String,
    val secret: String,
    val crypto: Crypto = Crypto.SHA1,
    val digits: Int = 6,
    val type: Type,
    val issuer: String = "",
    val epoch: Long = 0L,
    val period: Int = 30,
    val encoding: Encoding = Encoding.BASE32,
    var step: Long = 0L
) {
    enum class Type { TOTP, HOTP }
    enum class Encoding { BASE32, HEX }

    fun getSecret(): ByteArray {
        return (if (encoding == Encoding.BASE32) Base32().decode(secret) else Hex.decodeHex(secret))
    }

    fun incrementStep() {
        if (type == Type.HOTP) step++
    }
}