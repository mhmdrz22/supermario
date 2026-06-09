package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resistance_tasks")
data class ResistanceTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val threatLevel: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val status: String,      // "PENDING", "ACTIVE", "COMPLETED", "COMPROMISED"
    val targetSector: String,
    val decryptionKey: String, // AES decryption credentials key visual
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transmission_logs")
data class TransmissionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val encryptedContent: String,
    val decryptedContent: String,
    val isDecrypted: Boolean = false,
    val signalStrength: Int = 100, // percentage 0-100 indicating visual data integrity
    val timestamp: Long = System.currentTimeMillis()
)
