package com.example.securevault.crypto

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("secure_vault_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SALT = "auth_salt"
        private const val KEY_HASH = "auth_hash"
        private const val ITERATIONS = 100000
        private const val KEY_LENGTH = 256
    }

    fun isSetup(): Boolean {
        return prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)
    }

    fun setupMasterPassword(password: String): Boolean {
        return try {
            val salt = ByteArray(32)
            SecureRandom().nextBytes(salt)

            val hash = hashPassword(password, salt)

            prefs.edit()
                .putString(KEY_SALT, Base64.getEncoder().encodeToString(salt))
                .putString(KEY_HASH, Base64.getEncoder().encodeToString(hash))
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun verifyMasterPassword(password: String): Boolean {
        return try {
            val saltString = prefs.getString(KEY_SALT, null) ?: return false
            val storedHashString = prefs.getString(KEY_HASH, null) ?: return false

            val salt = Base64.getDecoder().decode(saltString)
            val storedHash = Base64.getDecoder().decode(storedHashString)

            val computedHash = hashPassword(password, salt)

            // Constant-time comparison to prevent timing attacks
            if (storedHash.size != computedHash.size) return false
            var result = 0
            for (i in storedHash.indices) {
                result = result or (storedHash[i].toInt() xor computedHash[i].toInt())
            }
            result == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
