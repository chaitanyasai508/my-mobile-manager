package com.example.securevault.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher = Cipher.getInstance(TRANSFORMATION)
    private val decryptCipher = Cipher.getInstance(TRANSFORMATION)

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray): Pair<ByteArray, ByteArray> {
        encryptCipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = encryptCipher.iv
        val encrypted = encryptCipher.doFinal(bytes)
        return Pair(iv, encrypted)
    }

    fun decrypt(iv: ByteArray, encryptedBytes: ByteArray): ByteArray {
        val spec = GCMParameterSpec(128, iv)
        decryptCipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return decryptCipher.doFinal(encryptedBytes)
    }

    companion object {
        private const val ALIAS = "secret_vault_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
