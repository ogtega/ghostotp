package de.tolunla.ghostotp

import android.text.format.DateUtils
import de.tolunla.ghostotp.model.OneTimePassword
import org.apache.commons.codec.binary.Hex
import org.junit.Assert.assertEquals
import org.junit.Test

class OneTimePasswordTest {

    private val secret = "3132333435363738393031323334353637383930"

    @Test
    fun rfc4226AppendixDTest() {
        val sha1 = OneTimePassword(crypto = "SHA1")
        val seed = Hex.decodeHex(secret)

        val results = intArrayOf(
            755224, 287082, 359152, 969429, 338314,
            254676, 287922, 162583, 399871, 520489
        )

        for (i in 0..9) {
            assertEquals(results[i], sha1.generateHOTP(seed, i.toLong()))
        }
    }

    @Test
    fun rfc6238AppendixBTest() {
        val sha1 = OneTimePassword(digits = 8, crypto = "SHA1")
        val sha256 = OneTimePassword(digits = 8, crypto = "SHA256")
        val shaSHA512 = OneTimePassword(digits = 8, crypto = "SHA512")

        val seed = Hex.decodeHex(secret)
        val seed32 = Hex.decodeHex(secret.repeat(2).slice(0..63))
        val seed64 = Hex.decodeHex(secret.repeat(4).slice(0..127))

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

            assertEquals(results[0][i], sha1.generateTOTP(seed, testTime))
            assertEquals(results[1][i], sha256.generateTOTP(seed32, testTime))
            assertEquals(results[2][i], shaSHA512.generateTOTP(seed64, testTime))
        }
    }
}