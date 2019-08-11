package de.tolunla.ghostotp.model

import androidx.room.Entity

@Entity(tableName = "account_table")
data class Account(
    val label: String,
    val secret: String,
    val crypto: String = "SHA1",
    val issuer: String,
    val type: Int,
    val counter: Long = 0L,
    val period: Int = 30
)