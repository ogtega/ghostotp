package de.tolunla.ghostotp.model

import de.tolunla.ghostotp.db.entity.AccountEntity
import de.tolunla.ghostotp.db.entity.AccountEntity.Type

abstract class Account(val id: Int?, val name: String, val issuer: String, val type: Type) {

    fun getLabel(): String {
        if (issuer.isEmpty()) return name
        return "$issuer ($name)"
    }

    fun toEntity() = AccountEntity(id, name, issuer, getJSON(), type)

    abstract fun getProgress(): Float

    abstract fun generateCode(): String

    abstract fun getJSON(): String
}