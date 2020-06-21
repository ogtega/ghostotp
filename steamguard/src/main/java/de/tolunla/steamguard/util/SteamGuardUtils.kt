package de.tolunla.steamguard.util

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

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

    val byteData = ByteBuffer.allocate(8)
    byteData.putInt(4, getCurrentTime(offset).toInt() / 30)
    byteData.order(ByteOrder.BIG_ENDIAN)

    val raw = mac.doFinal(byteData.array())
    val start = (raw[19] and 0x0F)

    val bytes = ByteBuffer.wrap(raw.slice(start..start + 4).toByteArray())

    var fullCode = bytes.int and 0x7fffffff and -0x1

    for (i in 0..4) {
        code += STEAMCHARS[fullCode.rem(STEAMCHARS.size)]
        fullCode = fullCode.div(STEAMCHARS.size)
    }

    return code
}
