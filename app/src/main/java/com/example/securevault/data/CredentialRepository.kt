package com.example.securevault.data

import com.example.securevault.crypto.CryptoManager
import com.example.securevault.crypto.ExportImportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Base64

class CredentialRepository(
    private val credentialDao: CredentialDao,
    private val cryptoManager: CryptoManager
) {

    val allCredentials: Flow<List<ExportImportManager.PlainCredential>> = credentialDao.getAllCredentials().map { entities ->
        entities.map { entity ->
            decryptCredential(entity)
        }
    }
    
    // We also need a way to get the raw entities for internal ID management if needed, 
    // but for the UI we mostly care about the decrypted data. 
    // However, to update/delete, we need the ID.
    // Let's create a Domain Model that includes the ID.
    
    data class DomainCredential(
        val id: Int,
        val title: String,
        val username: String,
        val password: String,
        val notes: String
    )

    val domainCredentials: Flow<List<DomainCredential>> = credentialDao.getAllCredentials().map { entities ->
        entities.map { entity ->
            val plain = decryptCredential(entity)
            DomainCredential(entity.id, plain.title, plain.username, plain.password, plain.notes)
        }
    }

    suspend fun getCredential(id: Int): DomainCredential? {
        val entity = credentialDao.getCredentialById(id) ?: return null
        val plain = decryptCredential(entity)
        return DomainCredential(entity.id, plain.title, plain.username, plain.password, plain.notes)
    }

    suspend fun insertCredential(title: String, username: String, password: String, notes: String) {
        val entity = encryptCredential(title, username, password, notes)
        credentialDao.insertCredential(entity)
    }

    suspend fun updateCredential(id: Int, title: String, username: String, password: String, notes: String) {
        val entity = encryptCredential(title, username, password, notes).copy(id = id)
        credentialDao.updateCredential(entity)
    }

    suspend fun deleteCredential(id: Int) {
        // We need to fetch it first to get the object to delete, or just delete by ID if we add a query for it.
        // Room Delete expects an entity.
        val entity = credentialDao.getCredentialById(id)
        if (entity != null) {
            credentialDao.deleteCredential(entity)
        }
    }
    
    suspend fun deleteAll() {
        credentialDao.deleteAll()
    }

    private fun encryptCredential(title: String, username: String, password: String, notes: String): Credential {
        val (usernameIv, encryptedUsername) = cryptoManager.encrypt(username.toByteArray())
        val (passwordIv, encryptedPassword) = cryptoManager.encrypt(password.toByteArray())
        val (notesIv, encryptedNotes) = cryptoManager.encrypt(notes.toByteArray())

        return Credential(
            title = title,
            usernameIv = Base64.getEncoder().encodeToString(usernameIv),
            encryptedUsername = Base64.getEncoder().encodeToString(encryptedUsername),
            passwordIv = Base64.getEncoder().encodeToString(passwordIv),
            encryptedPassword = Base64.getEncoder().encodeToString(encryptedPassword),
            notesIv = Base64.getEncoder().encodeToString(notesIv),
            encryptedNotes = Base64.getEncoder().encodeToString(encryptedNotes)
        )
    }

    private fun decryptCredential(entity: Credential): ExportImportManager.PlainCredential {
        val username = cryptoManager.decrypt(
            Base64.getDecoder().decode(entity.usernameIv),
            Base64.getDecoder().decode(entity.encryptedUsername)
        ).toString(Charsets.UTF_8)

        val password = cryptoManager.decrypt(
            Base64.getDecoder().decode(entity.passwordIv),
            Base64.getDecoder().decode(entity.encryptedPassword)
        ).toString(Charsets.UTF_8)

        val notes = cryptoManager.decrypt(
            Base64.getDecoder().decode(entity.notesIv),
            Base64.getDecoder().decode(entity.encryptedNotes)
        ).toString(Charsets.UTF_8)

        return ExportImportManager.PlainCredential(
            title = entity.title,
            username = username,
            password = password,
            notes = notes
        )
    }
}
