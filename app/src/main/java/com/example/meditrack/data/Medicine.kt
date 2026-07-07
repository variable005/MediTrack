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
    val lastTakenTimestamp: Long = 0 // To track if taken today
)