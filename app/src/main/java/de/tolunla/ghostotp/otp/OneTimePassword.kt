package de.tolunla.ghostotp.otp

import android.text.format.DateUtils
import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.db.entity.AccountEntity.Type
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

private val DIGITS_POWER = arrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

abstract class OneTimePassword(
    private val secret: ByteArray,
    private val digits: Int,
    private val crypto: Crypto = Crypto.SHA1
) {
    enum class Crypto(val type: String) {
        SHA1("SHA1"),
        SHA256("SHA256"),
        SHA512("SHA512")
    }

    companion object {
        fun newInstance(account: AccountEntity): OneTimePassword = (
                if (account.type == Type.HOTP)
                    HOTPassword(account)
                else
                    TOTPassword(account)
                )
    }

    abstract fun generateCode(): Int

    fun generateCode(step: Long): Int = getHash(secret, step).truncate()

    private fun getHash(bytes: ByteArray, steps: Long): ByteArray =
        Mac.getInstance("Hmac${crypto.type}").run {
            init(SecretKeySpec(bytes, "RAW"))
            doFinal(steps.getByteArray())
        }

    private fun ByteArray.truncate(): Int {
        val offset = (this.last().toInt() and 0xf)
        val binary = ByteBuffer.wrap(this.sliceArray(offset..offset + 4))

        binary.put(0, binary[0].and(0x7F))

        return binary.int.rem(DIGITS_POWER[digits])
    }

    private fun Long.getByteArray(): ByteArray {
        return ByteBuffer.allocate(8).putLong(this).array()
    }
}

class HOTPassword(private val account: AccountEntity) :
    OneTimePassword(account.getSecretBytes(), account.digits, account.crypto) {

    override fun generateCode(): Int {
        val result = generateCode(account.step)
        account.incrementStep()
        return result
    }
}

class TOTPassword(private val account: AccountEntity) :
    OneTimePassword(account.getSecretBytes(), account.digits, account.crypto) {

    override fun generateCode(): Int = generateCode(System.currentTimeMillis().toSteps())

    fun generateCodeAt(time: Long) = generateCode(time.toSteps())

    private fun Long.toSteps(): Long {
        val elapsed = (this - account.epoch) / DateUtils.SECOND_IN_MILLIS
        return elapsed / account.period
    }
}