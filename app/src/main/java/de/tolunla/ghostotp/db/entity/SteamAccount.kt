package de.tolunla.ghostotp.db.entity

import androidx.room.Entity

@Entity(tableName = "steam_accounts")
data class SteamAccount(val account_name: String, val shared_secret: String,
val serial_number: String, val revocation_code: String, val uri: String, val server_time: Long,
val token_gid: String, val identity_secret: String, val secret_1: String, val status: Int,
                        val device_id: String, val steamID: String) {
}