package de.tolunla.ghostotp.model

import android.text.format.DateUtils
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow

/**
 * @param digits: Length of code generated
 * @param interval: Length for each time step
 * @param epoch: Step calculation start time
 * @param crypto: Hashing algorithm used with HMAC
 */
class OneTimePassword(
    private val digits: Int = 6,
    private val interval: Int = 30,
    private val epoch: Long = 0L,
    private val crypto: String = "SHA256"
) {

    fun generateTOTP(bytes: ByteArray, time: Long = System.currentTimeMillis()): Int {
        return generateHOTP(bytes, getSteps(time))
    }

    fun generateHOTP(bytes: ByteArray, steps: Long): Int {
        return hmacHash(bytes, steps).truncate()
    }

    fun hmacHash(bytes: ByteArray, steps: Long): ByteArray {
        return Mac.getInstance("Hmac$crypto").run {
            init(SecretKeySpec(bytes, "RAW"))
            doFinal(steps.getByteArray())
        }
    }

    fun getSteps(time: Long): Long {
        val elapsed = (time - epoch) / DateUtils.SECOND_IN_MILLIS
        return elapsed / interval
    }

    private fun ByteArray.truncate(): Int {
        val offset = (this.last().toInt() and 0xf)
        val binary = ByteBuffer.wrap(this.sliceArray(offset..offset + 4))

        binary.put(0, binary[0].and(0x7F))

        return binary.int.rem(10.toDouble().pow(digits).toInt())
    }

    private fun Long.getByteArray(): ByteArray {
        return ByteBuffer.allocate(8).putLong(this).array()
    }
}