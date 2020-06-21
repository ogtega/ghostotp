package de.tolunla.ghostotp.model

import android.text.format.DateUtils
import de.tolunla.ghostotp.db.entity.AccountEntity.Type
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex
import org.json.JSONObject
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow

/**
 * Class used to represent all rfc4226 and rfc6238 One Time Passwords
 */
class OTPAccount(
    id: Long? = null,
    name: String,
    val secret: String,
    val crypto: String = "SHA1",
    val digits: Int = 6,
    type: Type,
    issuer: String = "",
    val epoch: Long = 0L,
    val period: Int = 30,
    val hex: Boolean = false,
    var step: Long = -1L
) : Account(id, name, issuer, type) {

    override fun getProgress(): Float {
        if (type == Type.HOTP) return 0f

        return (System.currentTimeMillis() - (epoch * DateUtils.SECOND_IN_MILLIS))
            .rem(period * DateUtils.SECOND_IN_MILLIS)
            .toFloat() / (period * DateUtils.SECOND_IN_MILLIS)
            .toFloat()
    }

    override fun generateCode(): String {
        val bytes = if (!hex) Base32().decode(secret) else Hex.decodeHex(secret)

        val code = if (type == Type.HOTP) {
            getHash(bytes, step).truncate()
        } else {
            getHash(bytes, System.currentTimeMillis().toSteps()).truncate()
        }

        return code.addPadding(digits)
    }

    override fun getJSON() = JSONObject(
        mapOf(
            "secret" to secret,
            "crypto" to crypto,
            "digits" to digits,
            "epoch" to epoch,
            "period" to period,
            "step" to step,
            "hex" to hex
        )
    ).toString()

    private fun getHash(bytes: ByteArray, steps: Long): ByteArray =
        Mac.getInstance("Hmac$crypto").run {
            init(SecretKeySpec(bytes, "RAW"))
            doFinal(steps.getByteArray())
        }

    private fun ByteArray.truncate(): Int {
        val offset = (this.last().toInt() and 0xf)
        val binary = ByteBuffer.wrap(this.sliceArray(offset..offset + 4))

        binary.put(0, binary[0].and(0x7F))

        return binary.int.rem(10.0.pow(digits).toInt())
    }

    private fun Long.toSteps(): Long {
        val elapsed = (this - epoch) / DateUtils.SECOND_IN_MILLIS
        return elapsed / period
    }

    private fun Long.getByteArray(): ByteArray {
        return ByteBuffer.allocate(8).putLong(this).array()
    }

    private fun Int.addPadding(digits: Int): String {
        val code = this.toString()
        return "${"0".repeat(digits - code.length)}$code"
    }
}