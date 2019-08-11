package de.tolunla.ghostotp

import android.text.format.DateUtils
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.Account.Encoding
import de.tolunla.ghostotp.model.Account.Type
import de.tolunla.ghostotp.otp.HOTPassword
import de.tolunla.ghostotp.otp.OneTimePassword.Crypto
import de.tolunla.ghostotp.otp.TOTPassword
import org.junit.Assert.assertEquals
import org.junit.Test

class OneTimePasswordTest {

    private val seed = "3132333435363738393031323334353637383930"

    @Test
    fun rfc4226AppendixDTest() {
        val results = intArrayOf(
            755224, 287082, 359152, 969429, 338314,
            254676, 287922, 162583, 399871, 520489
        )

        for (i in 0 until results.size) {
            val account = Account(
                "", seed, type = Type.HOTP, step = i.toLong(), encoding = Encoding.HEX
            )

            assertEquals(results[i], HOTPassword(account).generateCode())
        }
    }

    @Test
    fun rfc6238AppendixBTest() {
        val seed32 = seed.repeat(2).slice(0..63)
        val seed64 = seed.repeat(4).slice(0..127)

        val times = longArrayOf(
            59L, 1111111109L, 1111111111L,
            1234567890L, 2000000000L, 20000000000L
        )

        val results = arrayOf(
            intArrayOf(94287082, 7081804, 14050471, 89005924, 69279037, 65353130),
            intArrayOf(46119246, 68084774, 67062674, 91819424, 90698825, 77737706),
            intArrayOf(90693936, 25091201, 99943326, 93441116, 38618901, 47863826)
        )

        for (i in 0 until times.size) {
            val testTime = times[i] * DateUtils.SECOND_IN_MILLIS

            val sha1 = Account("", seed, digits = 8, type = Type.TOTP, encoding = Encoding.HEX)

            val sha256 = Account(
                "", seed32, crypto = Crypto.SHA256,
                digits = 8, type = Type.TOTP, encoding = Encoding.HEX
            )

            val sha512 = Account(
                "", seed64, crypto = Crypto.SHA512,
                digits = 8, type = Type.TOTP, encoding = Encoding.HEX
            )

            assertEquals(results[0][i], TOTPassword(sha1).generateCodeAt(testTime))
            assertEquals(results[1][i], TOTPassword(sha256).generateCodeAt(testTime))
            assertEquals(results[2][i], TOTPassword(sha512).generateCodeAt(testTime))
        }
    }
}