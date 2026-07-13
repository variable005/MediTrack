package com.example.meditrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate // <-- ADD THIS LINE

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long // <-- UPDATED

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("SELECT * FROM medicines ORDER BY reminderTime ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Int): Medicine?

    @Query("SELECT * FROM medicines WHERE endDate >= :today ORDER BY reminderTime ASC")
    fun getActiveMedicines(today: String = LocalDate.now().toString()): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE expiryDate < :today ORDER BY expiryDate DESC")
    fun getExpiredMedicines(today: String = LocalDate.now().toString()): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE endDate >= :today AND isPaused = 0")
    suspend fun getAllActiveForReschedule(today: String = LocalDate.now().toString()): List<Medicine>
}
