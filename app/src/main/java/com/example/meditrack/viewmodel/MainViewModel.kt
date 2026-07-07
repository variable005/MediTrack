package com.example.meditrack.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.Medicine
import com.example.meditrack.data.MedicineRepository
import com.example.meditrack.reminders.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

// Enum for Time Grouping
enum class TimeOfDay {
    Morning, Afternoon, Night
}

class MainViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    // --- Updated Helper function to categorize time ---
    private fun getGroupForTime(time: LocalTime): TimeOfDay {
        return when (time.hour) {
            in 5..11 -> TimeOfDay.Morning    // 5:00 AM - 11:59 AM
            in 12..17 -> TimeOfDay.Afternoon // 12:00 PM - 5:59 PM
            else -> TimeOfDay.Night        // 6:00 PM - 4:59 AM
        }
    }

    // --- StateFlow for grouped medicines using Enum ---
    val groupedActiveMedicines: StateFlow<Map<TimeOfDay, List<Medicine>>> = repository.activeMedicines
        .map { medicines ->
            // Group by the enum
            val grouped = medicines.groupBy { getGroupForTime(it.reminderTime) }
            // Ensure all keys exist, even if empty, and sort correctly
            val sortedMap = sortedMapOf<TimeOfDay, List<Medicine>>()
            sortedMap[TimeOfDay.Morning] = grouped.getOrDefault(TimeOfDay.Morning, emptyList())
            sortedMap[TimeOfDay.Afternoon] = grouped.getOrDefault(TimeOfDay.Afternoon, emptyList())
            sortedMap[TimeOfDay.Night] = grouped.getOrDefault(TimeOfDay.Night, emptyList())
            sortedMap
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = mapOf( // Start with all keys present
                TimeOfDay.Morning to emptyList(),
                TimeOfDay.Afternoon to emptyList(),
                TimeOfDay.Night to emptyList()
            )
        )

    // Public access for HistoryScreen
    val activeMedicines: StateFlow<List<Medicine>> = repository.activeMedicines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expiredMedicines: StateFlow<List<Medicine>> = repository.expiredMedicines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMedicine(
        context: Context, name: String, dosage: String, reminderTime: LocalTime,
        startDate: LocalDate, endDate: LocalDate, expiryDate: LocalDate
    ) {
        viewModelScope.launch {
            val newMedicine = Medicine(
                name = name, dosage = dosage, reminderTime = reminderTime,
                startDate = startDate, endDate = endDate, expiryDate = expiryDate
            )
            val newId = repository.insert(newMedicine)
            val insertedMedicine = repository.getMedicineById(newId.toInt())
            if (insertedMedicine != null) {
                AlarmScheduler.scheduleReminder(context, insertedMedicine)
            }
        }
    }

    fun markAsTaken(context: Context, medicine: Medicine) {
        viewModelScope.launch {
            val updatedMedicine = medicine.copy(lastTakenTimestamp = System.currentTimeMillis())
            repository.update(updatedMedicine)
            AlarmScheduler.cancelReminder(context, medicine)
            AlarmScheduler.scheduleReminder(context, updatedMedicine)
        }
    }
}

// Factory remains the same
class MainViewModelFactory(private val repository: MedicineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}