package com.example.securevault.data

import com.example.securevault.crypto.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Base64

class NoteRepository(
    private val noteDao: NoteDao,
    private val cryptoManager: CryptoManager
) {

    data class DomainNote(
        val id: Int,
        val title: String,
        val content: String,
        val timestamp: Long
    )

    val domainNotes: Flow<List<DomainNote>> = noteDao.getAllNotes().map { entities ->
        entities.map { entity ->
            val content = decryptField(entity.contentIv, entity.encryptedContent)
            DomainNote(entity.id, entity.title, content, entity.timestamp)
        }
    }

    suspend fun getNote(id: Int): DomainNote? {
        val entity = noteDao.getNoteById(id) ?: return null
        val content = decryptField(entity.contentIv, entity.encryptedContent)
        return DomainNote(entity.id, entity.title, content, entity.timestamp)
    }

    suspend fun insertNote(title: String, content: String) {
        val entity = encryptNote(title, content)
        noteDao.insertNote(entity)
    }

    suspend fun updateNote(id: Int, title: String, content: String) {
        val entity = encryptNote(title, content).copy(id = id)
        noteDao.updateNote(entity)
    }

    suspend fun deleteNote(id: Int) {
        val entity = noteDao.getNoteById(id)
        if (entity != null) {
            noteDao.deleteNote(entity)
        }
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    private fun encryptNote(title: String, content: String): Note {
        val (contentIv, encryptedContent) = cryptoManager.encrypt(content.toByteArray())

        return Note(
            title = title,
            contentIv = Base64.getEncoder().encodeToString(contentIv),
            encryptedContent = Base64.getEncoder().encodeToString(encryptedContent)
        )
    }

    private fun decryptField(ivBase64: String, encryptedBase64: String): String {
        return cryptoManager.decrypt(
            Base64.getDecoder().decode(ivBase64),
            Base64.getDecoder().decode(encryptedBase64)
        ).toString(Charsets.UTF_8)
    }
}
