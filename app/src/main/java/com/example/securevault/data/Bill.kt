package com.example.securevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val billName: String,
    val amountIv: String, // Base64 encoded IV
    val encryptedAmount: String, // Base64 encoded encrypted data
    val notesIv: String, // Base64 encoded IV
    val encryptedNotes: String, // Base64 encoded encrypted data
    val dueDate: Long, // Timestamp in milliseconds
    val frequency: String, // "MONTHLY", "QUARTERLY", "SEMI_ANNUAL", "ANNUAL"
    val timestamp: Long = System.currentTimeMillis()
)
