package com.example.securevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class Credential(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val usernameIv: String, // Base64 encoded IV
    val encryptedUsername: String, // Base64 encoded encrypted data
    val passwordIv: String, // Base64 encoded IV
    val encryptedPassword: String, // Base64 encoded encrypted data
    val urlIv: String, // Base64 encoded IV
    val encryptedUrl: String, // Base64 encoded encrypted data
    val notesIv: String, // Base64 encoded IV
    val encryptedNotes: String, // Base64 encoded encrypted data
    val timestamp: Long = System.currentTimeMillis()
)
