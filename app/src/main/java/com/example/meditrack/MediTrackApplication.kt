package com.example.meditrack

import android.app.Application
import com.example.meditrack.data.AppDatabase
import com.example.meditrack.data.MedicineRepository

class MediTrackApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MedicineRepository(database.medicineDao(), database.scheduleDao(), database.doseLogDao()) }
}
