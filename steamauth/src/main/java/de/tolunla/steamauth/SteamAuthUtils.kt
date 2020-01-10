package de.tolunla.steamauth

import org.apache.commons.codec.digest.DigestUtils

class SteamAuthUtils {
  companion object {
    fun getDeviceId(steamID: String): String {
      val raw = DigestUtils.sha1Hex(steamID)
      return "android:${raw.substring(0..7)}" +
          "-${raw.substring(8..11)}" +
          "-${raw.substring(12..15)}" +
          "-${raw.substring(16..19)}" +
          "-${raw.substring(20..31)}"
    }
  }
}