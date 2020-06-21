package de.tolunla.ghostotp.util

import android.net.Uri
import de.tolunla.ghostotp.model.Account
import de.tolunla.ghostotp.db.entity.AccountEntity.Type
import de.tolunla.ghostotp.model.OTPAccount
import de.tolunla.ghostotp.model.SteamAccount

class AccountUtils {
    companion object {

        fun accountFromUri(uri: Uri): Account? {
            var name = ""

            "/.*:.*".toRegex().find(uri.path ?: "")?.let {
                name = it.groupValues[2]
            }

            if (name.isEmpty()) return null

            return when (uri.scheme) {
                "totp" -> otpFromUri(uri, name, Type.TOTP)
                "hotp" -> otpFromUri(uri, name, Type.HOTP)
                "steam" -> steamFromUri(uri, name)
                else -> throw Exception()
            }
        }

        private fun otpFromUri(uri: Uri, name: String, type: Type): OTPAccount {
            val secret = uri.getQueryParameter("secret") ?: ""

            return OTPAccount(
                issuer = uri.getQueryParameter("issuer") ?: "",
                name = name,
                type = type,
                secret = secret,
                step = (uri.getQueryParameter("counter") ?: "-1").toLong()
            )
        }

        private fun steamFromUri(uri: Uri, name: String): SteamAccount {
            val secret = uri.getQueryParameter("secret") ?: ""

            return SteamAccount(
                name = name,
                sharedSecret = secret,
                revocationCode = uri.getQueryParameter("revocation_code") ?: "",
                identitySecret = uri.getQueryParameter("identity_secret") ?: ""
            )
        }
    }
}