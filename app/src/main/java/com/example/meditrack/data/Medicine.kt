package com.example.meditrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dosage: String,
    val reminderTime: LocalTime,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val expiryDate: LocalDate,
    val lastTakenTimestamp: Long = 0,
    val instructions: String = "",
    val notes: String = "",
    val imageUri: String? = null,
    val initialQuantity: Int = 0,
    val remainingQuantity: Int = 0,
    val refillThreshold: Int = 0,
    val isPaused: Boolean = false
)
