package de.tolunla.ghostotp.db

import android.security.keystore.KeyProperties
import javax.crypto.Cipher

class AppCipher {

    companion object {

        @Volatile
        private var INSTANCE: Cipher? = null

        fun getInstance(): Cipher = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES +
                    "/${KeyProperties.BLOCK_MODE_CBC}" +
                    "/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            )
        }

        fun setInstance(cipher: Cipher) {
            synchronized(this) {
                INSTANCE = cipher
            }
        }
    }
}
