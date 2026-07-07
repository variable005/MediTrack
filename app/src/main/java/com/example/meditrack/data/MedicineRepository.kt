package com.example.meditrack.data

import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val medicineDao: MedicineDao) {

    val allMedicines: Flow<List<Medicine>> = medicineDao.getAllMedicines()
    val activeMedicines: Flow<List<Medicine>> = medicineDao.getActiveMedicines()
    val expiredMedicines: Flow<List<Medicine>> = medicineDao.getExpiredMedicines()

    suspend fun insert(medicine: Medicine): Long { // <-- UPDATED
        return medicineDao.insert(medicine)
    }

    suspend fun update(medicine: Medicine) {
        medicineDao.update(medicine)
    }

    suspend fun getMedicineById(id: Int): Medicine? {
        return medicineDao.getMedicineById(id)
    }

    suspend fun getAllActiveForReschedule(): List<Medicine> {
        return medicineDao.getAllActiveForReschedule()
    }
}