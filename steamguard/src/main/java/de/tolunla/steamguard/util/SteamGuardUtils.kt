package de.tolunla.steamguard.util

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.min

private val STEAMCHARS = charArrayOf(
    '2', '3', '4', '5', '6', '7', '8', '9', 'B', 'C',
    'D', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q',
    'R', 'T', 'V', 'W', 'X', 'Y'
)

private fun getCurrentTime(offset: Int): Long {
    return (System.currentTimeMillis() / 1000L) + offset
}

/**
 * Generates the 2FA code for SteamGuard
 * @return the 2FA code.
 */
fun generateAuthCode(secret: String, offset: Int): String {
    var code = ""
    val keyBytes = Base64.decode(secret, Base64.DEFAULT)
    val keySpec = SecretKeySpec(keyBytes, "HmacSHA1")

    val mac = Mac.getInstance("HmacSHA1")
    mac.init(keySpec)

    val buffer = ByteBuffer.allocate(8)
    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(4, getCurrentTime(offset).toInt() / 30)

    val raw = mac.doFinal(buffer.array())
    val start = (raw[19] and 0x0F)

    val bytes = ByteBuffer.wrap(raw.slice(start..start + 4).toByteArray())

    var fullCode = bytes.int and 0x7fffffff and -0x1

    for (i in 0..4) {
        code += STEAMCHARS[fullCode.rem(STEAMCHARS.size)]
        fullCode = fullCode.div(STEAMCHARS.size)
    }

    return code
}

/**
 * Generates a confirmation key used for trades
 * @return the confirmation key.
 */
fun generateConfKey(identitySecret: String, time: Long, tag: String = "conf"): String {
    val keyBytes = Base64.decode(identitySecret, Base64.DEFAULT)
    val keySpec = SecretKeySpec(keyBytes, "HmacSHA1")

    val mac = Mac.getInstance("HmacSHA1")
    mac.init(keySpec)

    val buffer = ByteBuffer.allocate(8 + min(tag.length, 32))
    buffer.order(ByteOrder.BIG_ENDIAN)

    buffer.putLong(time)
    buffer.put(tag.toByteArray())

    val raw = mac.doFinal(buffer.array())

    return Base64.encodeToString(raw, Base64.NO_WRAP)
}
