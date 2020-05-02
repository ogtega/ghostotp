package de.tolunla.ghostotp.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import de.tolunla.ghostotp.db.entity.AccountEntity.Type.*
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.model.OTPAccount
import org.json.JSONObject

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String, val issuer: String = "", val json: String, val type: Type
) {
    enum class Type { HOTP, TOTP, STEAM }

    @Ignore
    val data = JSONObject(json)

    fun getAccount(): Account = when (type) {
        HOTP -> OTPAccount(
            id = id,
            name = name,
            secret = data.getString("secret"),
            crypto = data.optString("crypto", "SHA1"),
            digits = data.optInt("digits", 6),
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
            type = type,
            issuer = issuer,
            epoch = data.optLong("epoch", 0L),
            period = data.optInt("period", 30),
            step = -1L
        )
        STEAM -> TODO()
    }

    class TypeStringConverter {
        @androidx.room.TypeConverter
        fun typeToString(type: Type): String {
            return type.name
        }

        @androidx.room.TypeConverter
        fun stringToType(str: String): Type {
            return valueOf(str)
        }
    }
}