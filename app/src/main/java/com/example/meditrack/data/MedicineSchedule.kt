package com.example.meditrack.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

/** One locally scheduled dose. Days are ISO day numbers (1=Monday), comma-separated. */
@Entity(
    tableName = "medicine_schedules",
    foreignKeys = [ForeignKey(entity = Medicine::class, parentColumns = ["id"], childColumns = ["medicineId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("medicineId")]
)
data class MedicineSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val reminderTime: LocalTime,
    val daysOfWeek: String = "1,2,3,4,5,6,7",
    val intervalDays: Int = 1,
    val isEnabled: Boolean = true
)
