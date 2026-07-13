package com.example.meditrack.data

import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val medicineDao: MedicineDao, private val scheduleDao: ScheduleDao, private val doseLogDao: DoseLogDao) {

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

    suspend fun delete(medicine: Medicine) {
        medicineDao.delete(medicine)
    }

    fun schedulesForMedicine(id: Int) = scheduleDao.schedulesForMedicine(id)
    suspend fun schedulesForMedicineOnce(id: Int) = scheduleDao.schedulesForMedicineOnce(id)
    suspend fun enabledSchedules() = scheduleDao.enabledSchedules()
    suspend fun replaceSchedules(medicineId: Int, schedules: List<MedicineSchedule>) {
        scheduleDao.deleteForMedicine(medicineId)
        scheduleDao.insertAll(schedules.map { it.copy(id = 0, medicineId = medicineId) })
    }
    suspend fun addSchedule(schedule: MedicineSchedule) = scheduleDao.insert(schedule)
    fun logsBetween(from: Long, to: Long) = doseLogDao.logsBetween(from, to)
    fun logsForMedicine(id: Int) = doseLogDao.logsForMedicine(id)
    suspend fun logDose(log: DoseLog) = doseLogDao.insert(log)
    suspend fun findLog(medicineId: Int, scheduledAt: Long) = doseLogDao.find(medicineId, scheduledAt)
}
