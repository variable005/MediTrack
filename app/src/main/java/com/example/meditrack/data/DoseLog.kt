package com.example.meditrack.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DoseStatus { TAKEN, SKIPPED, MISSED }

@Entity(
    tableName = "dose_logs",
    foreignKeys = [ForeignKey(entity = Medicine::class, parentColumns = ["id"], childColumns = ["medicineId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("medicineId"), Index(value = ["medicineId", "scheduledAt"], unique = true)]
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val scheduleId: Int? = null,
    val scheduledAt: Long,
    val recordedAt: Long = System.currentTimeMillis(),
    val status: DoseStatus,
    val note: String = ""
)
