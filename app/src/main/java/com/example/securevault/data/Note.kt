package com.example.securevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val contentIv: String, // Base64 encoded IV
    val encryptedContent: String, // Base64 encoded encrypted data
    val timestamp: Long = System.currentTimeMillis()
)
