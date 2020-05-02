package de.tolunla.ghostotp.model

import android.text.format.DateUtils
import de.tolunla.ghostotp.db.entity.AccountEntity.Type
import org.json.JSONObject
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

class OTPAccount(
    id: Int? = null,
    name: String,
    val secret: String,
    val crypto: String = "SHA1",
    val digits: Int = 6,
    type: Type,
    issuer: String = "",
    val epoch: Long = -1L,
    val period: Int = 30,
    var step: Long = -1L
) : Account(id, name, issuer, type) {

    private val DIGITS_POWER = arrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    override fun getProgress(): Float {
        if (type == Type.HOTP) return 0f

        return (System.currentTimeMillis() - (epoch * DateUtils.SECOND_IN_MILLIS))
            .rem(period * DateUtils.SECOND_IN_MILLIS)
            .toFloat() / (period * DateUtils.SECOND_IN_MILLIS)
            .toFloat()
    }

    override fun generateCode(): String {
        val code = if (type == Type.HOTP) {
            getHash(secret.toByteArray(), step).truncate()
        } else {
            getHash(secret.toByteArray(), System.currentTimeMillis().toSteps()).truncate()
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
            "step" to step
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

        return binary.int.rem(DIGITS_POWER[digits])
    }

    private fun Long.toSteps(): Long {
        val elapsed = (this - epoch) / DateUtils.SECOND_IN_MILLIS
        return elapsed / period
    }

    private fun Long.getByteArray(): ByteArray {
        return ByteBuffer.allocate(8).putLong(this).array()
    }

    /**
     * Method used to add n 0s before generated code (where n is the digits needed for a complete code)
     */
    private fun Int.addPadding(digits: Int): String {
        val code = this.toString()
        return "${"0".repeat(digits - code.length)}$code"
    }
}