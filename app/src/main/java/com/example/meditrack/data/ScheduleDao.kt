package com.example.meditrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM medicine_schedules WHERE medicineId = :medicineId ORDER BY reminderTime") fun schedulesForMedicine(medicineId: Int): Flow<List<MedicineSchedule>>
    @Query("SELECT * FROM medicine_schedules WHERE medicineId = :medicineId ORDER BY reminderTime") suspend fun schedulesForMedicineOnce(medicineId: Int): List<MedicineSchedule>
    @Query("SELECT * FROM medicine_schedules WHERE isEnabled = 1") suspend fun enabledSchedules(): List<MedicineSchedule>
    @Insert suspend fun insert(schedule: MedicineSchedule): Long
    @Insert suspend fun insertAll(schedules: List<MedicineSchedule>)
    @Update suspend fun update(schedule: MedicineSchedule)
    @Query("DELETE FROM medicine_schedules WHERE medicineId = :medicineId") suspend fun deleteForMedicine(medicineId: Int)
}
