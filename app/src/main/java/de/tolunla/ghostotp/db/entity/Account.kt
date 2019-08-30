package de.tolunla.ghostotp.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import de.tolunla.ghostotp.otp.OneTimePassword
import de.tolunla.ghostotp.otp.OneTimePassword.Crypto
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Hex
import java.util.*

@Entity(tableName = "accounts")
@Suppress("DataClassPrivateConstructor")
data class Account(
  val name: String, val secret: String, val crypto: Crypto = Crypto.SHA1, val digits: Int = 6,
  val type: Type, val issuer: String = "", val epoch: Long = 0L, val period: Int = 30,
  val hex: Boolean = false, var step: Long = 0L,
  @PrimaryKey val id: String = "${issuer.toLowerCase(Locale.ROOT)}$name"
) {

  enum class Type { TOTP, HOTP }

  @Ignore
  val oneTimePassword = OneTimePassword.newInstance(this)

  fun getSecretBytes(): ByteArray {
    return (if (!hex) Base32().decode(secret) else Hex.decodeHex(secret))
  }

  fun incrementStep() {
    if (type == Type.HOTP) step++
  }

  override fun equals(other: Any?): Boolean {
    other?.let {
      if (it is Account) {
        return it.id == id
      }
    }

    return false
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  class TypeStringConverter {
    @androidx.room.TypeConverter
    fun typeToString(type: Type): String {
      return type.name
    }

    @androidx.room.TypeConverter
    fun stringToType(str: String): Type {
      return Type.valueOf(str)
    }
  }

  class CryptoStringConverter {
    @androidx.room.TypeConverter
    fun cryptoToString(crypto: Crypto): String {
      return crypto.name
    }

    @androidx.room.TypeConverter
    fun stringToType(str: String): Crypto {
      return Crypto.valueOf(str)
    }
  }
}