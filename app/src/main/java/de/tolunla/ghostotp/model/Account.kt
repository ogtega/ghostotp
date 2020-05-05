package de.tolunla.ghostotp.model

import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.db.entity.AccountEntity.Type

/**
 * Abstract class representation of all accounts
 * @param id optional database id
 * @param name the name of the account
 * @param issuer the issuer of the account
 * @param type the type of the account
 */
abstract class Account(val id: Long?, val name: String, val issuer: String, val type: Type) {

    fun getLabel(): String {
        if (issuer.isEmpty()) return name
        return "$issuer ($name)"
    }

    /**
     * Converts the Account to an AccountEntity
     * @return a AccountEntity using the account info
     */
    fun toEntity() = AccountEntity(id, name, issuer, getJSON(), type)

    /**
     * Gets the progress till the next code update as a float
     * @return percent till next code.
     */
    abstract fun getProgress(): Float

    /**
     * Gets the current 2FA for the account
     * @return 2FA code.
     */
    abstract fun generateCode(): String

    /**
     * Gets the additional information from the current account
     * @return JSON string of additional info.
     */
    abstract fun getJSON(): String
}