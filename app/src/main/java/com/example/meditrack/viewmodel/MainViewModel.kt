package com.example.meditrack.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.Medicine
import com.example.meditrack.data.MedicineRepository
import com.example.meditrack.data.MedicineSchedule
import com.example.meditrack.data.DoseLog
import com.example.meditrack.data.DoseStatus
import com.example.meditrack.reminders.AlarmScheduler
import com.example.meditrack.widget.MedicineWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    application: Application,
    private val repository: MedicineRepository
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("meditrack_settings", Context.MODE_PRIVATE)

    // --- Settings flows ---
    private val _hapticEnabled = MutableStateFlow(sharedPreferences.getBoolean("haptic_enabled", true))
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPreferences.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _textSize = MutableStateFlow(sharedPreferences.getString("text_size", "medium") ?: "medium")
    val textSize: StateFlow<String> = _textSize.asStateFlow()

    private val _themeColor = MutableStateFlow(sharedPreferences.getString("theme_color", "teal") ?: "teal")
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    private val _userName = MutableStateFlow(sharedPreferences.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(sharedPreferences.getBoolean("onboarding_completed", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setHapticEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("haptic_enabled", enabled).apply()
        _hapticEnabled.value = enabled
    }

    fun setThemeMode(mode: String) {
        sharedPreferences.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setTextSize(size: String) {
        sharedPreferences.edit().putString("text_size", size).apply()
        _textSize.value = size
    }

    fun setThemeColor(color: String) {
        sharedPreferences.edit().putString("theme_color", color).apply()
        _themeColor.value = color
        MedicineWidgetProvider.updateAllWidgets(getApplication())
    }

    fun setUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean("onboarding_completed", completed).apply()
        _onboardingCompleted.value = completed
    }

    // --- Helper function to categorize time ---
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
            val scheduledMedicines = medicines.filterNot { it.isPaused }
            val grouped = scheduledMedicines.groupBy { getGroupForTime(it.reminderTime) }
            val sortedMap = sortedMapOf<TimeOfDay, List<Medicine>>()
            sortedMap[TimeOfDay.Morning] = grouped.getOrDefault(TimeOfDay.Morning, emptyList())
            sortedMap[TimeOfDay.Afternoon] = grouped.getOrDefault(TimeOfDay.Afternoon, emptyList())
            sortedMap[TimeOfDay.Night] = grouped.getOrDefault(TimeOfDay.Night, emptyList())
            sortedMap
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = mapOf(
                TimeOfDay.Morning to emptyList(),
                TimeOfDay.Afternoon to emptyList(),
                TimeOfDay.Night to emptyList()
            )
        )

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
        startDate: LocalDate, endDate: LocalDate, expiryDate: LocalDate,
        instructions: String = "", notes: String = "", quantity: Int = 0, refillThreshold: Int = 0
    ) {
        viewModelScope.launch {
            val newMedicine = Medicine(
                name = name, dosage = dosage, reminderTime = reminderTime,
                startDate = startDate, endDate = endDate, expiryDate = expiryDate,
                instructions = instructions, notes = notes, initialQuantity = quantity,
                remainingQuantity = quantity, refillThreshold = refillThreshold
            )
            val newId = repository.insert(newMedicine)
            val insertedMedicine = repository.getMedicineById(newId.toInt())
            if (insertedMedicine != null) {
                val scheduleId = repository.addSchedule(MedicineSchedule(medicineId = insertedMedicine.id, reminderTime = reminderTime)).toInt()
                val schedule = MedicineSchedule(id = scheduleId, medicineId = insertedMedicine.id, reminderTime = reminderTime)
                AlarmScheduler.scheduleReminder(context, insertedMedicine, schedule)
            }
            MedicineWidgetProvider.updateAllWidgets(context)
        }
    }

    fun markAsTaken(context: Context, medicine: Medicine) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updatedMedicine = medicine.copy(lastTakenTimestamp = now, remainingQuantity = (medicine.remainingQuantity - 1).coerceAtLeast(0))
            repository.update(updatedMedicine)
            repository.logDose(DoseLog(medicineId = medicine.id, scheduledAt = now, status = DoseStatus.TAKEN))
            repository.schedulesForMedicineOnce(medicine.id).forEach { schedule ->
                AlarmScheduler.cancelReminder(context, medicine, schedule)
                AlarmScheduler.scheduleReminder(context, updatedMedicine, schedule)
            }
            MedicineWidgetProvider.updateAllWidgets(context)
        }
     }

    fun setPaused(context: Context, medicine: Medicine, paused: Boolean) = viewModelScope.launch {
        val updated = medicine.copy(isPaused = paused)
        repository.update(updated)
        repository.schedulesForMedicineOnce(medicine.id).forEach { schedule ->
            AlarmScheduler.cancelReminder(context, medicine, schedule)
            if (!paused) AlarmScheduler.scheduleReminder(context, updated, schedule)
        }
    }

    fun addReminderTime(context: Context, medicine: Medicine, time: LocalTime) = viewModelScope.launch {
        val id = repository.addSchedule(MedicineSchedule(medicineId = medicine.id, reminderTime = time)).toInt()
        AlarmScheduler.scheduleReminder(context, medicine, MedicineSchedule(id = id, medicineId = medicine.id, reminderTime = time))
    }

    fun deleteMedicine(context: Context, medicine: Medicine) {
        viewModelScope.launch {
            repository.schedulesForMedicineOnce(medicine.id).forEach { AlarmScheduler.cancelReminder(context, medicine, it) }
            repository.delete(medicine)
            MedicineWidgetProvider.updateAllWidgets(context)
        }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val repository: MedicineRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
