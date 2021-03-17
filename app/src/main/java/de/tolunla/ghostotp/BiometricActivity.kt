package de.tolunla.ghostotp

import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import de.tolunla.ghostotp.db.AppCipher
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class BiometricActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val KEY_SIZE = 256
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private val KEY_NAME: String = "ghostotp"

    companion object {
        class BioCheckObserver(private val registry: ActivityResultRegistry) :
            DefaultLifecycleObserver {
            lateinit var contract: ActivityResultLauncher<Intent>
            lateinit var callback: (success: Boolean) -> Unit

            override fun onCreate(owner: LifecycleOwner) {
                contract = registry.register(
                    "biometrics",
                    ActivityResultContracts.StartActivityForResult()
                ) { result: ActivityResult ->
                    result.data?.extras?.get("authenticated").let {
                        if (it !is Boolean) callback.invoke(false)
                        else callback.invoke(it)
                    }
                }
            }

            fun bioCheck(
                context: ComponentActivity,
                opmode: Int = Cipher.DECRYPT_MODE,
                require: Boolean = false,
                callback: (success: Boolean) -> Unit
            ) {
                this.callback = callback
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)

                if ((!prefs.getBoolean(
                        context.getString(R.string.preference_biometrics_key),
                        false
                    ) && !require) || (!require && AppCipher.getInstance().iv != null)
                ) {
                    callback.invoke(true)
                    return
                }

                val authIntent = Intent(context, BiometricActivity::class.java)
                authIntent.putExtra("opmode", opmode)
                contract.launch(authIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = Intent()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        data.putExtra("authenticated", false)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    setResult(RESULT_CANCELED, data)
                    finish()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()

                    result.cryptoObject?.cipher?.let {
                        AppCipher.setInstance(it)
                        data.putExtra("authenticated", true)
                        setResult(RESULT_OK, data)
                    }

                    finish()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setNegativeButtonText(getString(R.string.action_cancel))
            .build()

        val cipher = Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES +
                "/${KeyProperties.BLOCK_MODE_CBC}" +
                "/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        )

        val secretKey = getOrCreateSecretKey()

        when (intent.getIntExtra("opmode", Cipher.ENCRYPT_MODE)) {
            Cipher.ENCRYPT_MODE -> cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            else -> {
                val iv = prefs.getString("CIPHER_IV", null)
                Log.d("TAG", iv ?: "None")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    IvParameterSpec(
                        iv?.toByteArray(Charsets.ISO_8859_1) ?: AppCipher.getInstance().iv
                    )
                )
            }
        }

        biometricPrompt.authenticate(
            promptInfo,
            BiometricPrompt.CryptoObject(cipher)
        )
    }

    private fun getOrCreateSecretKey(): SecretKey {
        // If Secretkey was previously created for that keyName, then grab and return it.
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(KEY_NAME, null)?.let { return it as SecretKey }

        // if you reach here, then a new SecretKey must be generated for that keyName
        val paramsBuilder = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )

        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            ENCRYPTION_ALGORITHM,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }
}