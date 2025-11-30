package com.example.securevault.crypto

import android.content.Context
import android.net.Uri
import com.example.securevault.data.Credential
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class ExportImportManager(private val context: Context) {

    private val gson = Gson()

    // Data class for the exported file structure
    data class ExportData(
        val salt: String, // Base64
        val iv: String,   // Base64
        val data: String  // Base64 encrypted JSON list of credentials
    )
    
    // Plain object to serialize before encryption
    data class PlainCredential(
        val title: String,
        val username: String,
        val password: String,
        val notes: String
    )

    fun exportData(uri: Uri, password: String, credentials: List<PlainCredential>): Boolean {
        return try {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)

            val keySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv

            val jsonString = gson.toJson(credentials)
            val encryptedBytes = cipher.doFinal(jsonString.toByteArray(Charsets.UTF_8))

            val exportData = ExportData(
                salt = Base64.getEncoder().encodeToString(salt),
                iv = Base64.getEncoder().encodeToString(iv),
                data = Base64.getEncoder().encodeToString(encryptedBytes)
            )

            val exportJson = gson.toJson(exportData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(exportJson)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importData(uri: Uri, password: String): List<PlainCredential>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val jsonString = InputStreamReader(inputStream).use { it.readText() }
            
            val exportData = gson.fromJson(jsonString, ExportData::class.java)
            
            val salt = Base64.getDecoder().decode(exportData.salt)
            val iv = Base64.getDecoder().decode(exportData.iv)
            val encryptedBytes = Base64.getDecoder().decode(exportData.data)

            val keySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val decryptedJson = String(decryptedBytes, Charsets.UTF_8)

            val type = object : TypeToken<List<PlainCredential>>() {}.type
            gson.fromJson(decryptedJson, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
