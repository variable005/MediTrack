package com.example.meditrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(log: DoseLog): Long
    @Query("SELECT * FROM dose_logs WHERE scheduledAt >= :from AND scheduledAt < :to ORDER BY scheduledAt DESC") fun logsBetween(from: Long, to: Long): Flow<List<DoseLog>>
    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId ORDER BY scheduledAt DESC") fun logsForMedicine(medicineId: Int): Flow<List<DoseLog>>
    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId AND scheduledAt = :scheduledAt LIMIT 1") suspend fun find(medicineId: Int, scheduledAt: Long): DoseLog?
}
