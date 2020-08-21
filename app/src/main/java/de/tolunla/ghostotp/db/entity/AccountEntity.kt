package de.tolunla.ghostotp.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import de.tolunla.ghostotp.db.entity.AccountEntity.Type.HOTP
import de.tolunla.ghostotp.db.entity.AccountEntity.Type.STEAM
import de.tolunla.ghostotp.db.entity.AccountEntity.Type.TOTP
import de.tolunla.ghostotp.db.entity.AccountEntity.Type.valueOf
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.OTPAccount
import de.tolunla.ghostotp.model.SteamAccount
import org.json.JSONObject

/**
 * Class representing an account table entry
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String,
    val issuer: String = "",
    val json: String,
    val type: Type
) {
    /**
     * Enum of all account types
     */
    enum class Type { HOTP, TOTP, STEAM }

    @Ignore
    val data = JSONObject(json)

    /**
     * Get the Account object the entity represents
     * @return Account object based off entity type and details
     */
    fun getAccount(): Account = when (type) {
        HOTP -> OTPAccount(
            id = id,
            name = name,
            secret = data.getString("secret"),
            crypto = data.optString("crypto", "SHA1"),
            digits = data.optInt("digits", 6),
            hex = data.optBoolean("hex", false),
            type = type,
            issuer = issuer,
            epoch = -1L,
            period = -1,
            step = data.optLong("step", 0L)
        )
        TOTP -> OTPAccount(
            id = id,
            name = name,
            secret = data.getString("secret"),
            crypto = data.optString("crypto", "SHA1"),
            digits = data.optInt("digits", 6),
            hex = data.optBoolean("hex", false),
            type = type,
            issuer = issuer,
            epoch = data.optLong("epoch", 0L),
            period = data.optInt("period", 30),
            step = -1L
        )
        STEAM -> SteamAccount(
            id = id,
            name = name,
            issuer = issuer,
            type = type,
            sharedSecret = data.getString("shared_secret"),
            revocationCode = data.getString("revocation_code"),
            identitySecret = data.getString("identity_secret")
        )
    }

    /**
     * Used by the DAO to convert between Type and String
     */
    class TypeStringConverter {
        /**
         * Converts Type to String
         * @param type Type enum to be converted
         * @return a string representation of a Type
         */
        @androidx.room.TypeConverter
        fun typeToString(type: Type): String {
            return type.name
        }

        /**
         * Converts String to Type
         * @param str String to be converted
         * @return a Type matching the string
         */
        @androidx.room.TypeConverter
        fun stringToType(str: String): Type {
            return valueOf(str)
        }
    }
}
