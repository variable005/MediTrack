package com.example.meditrack.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.meditrack.MediTrackApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val repository = (context.applicationContext as MediTrackApplication).repository

            CoroutineScope(Dispatchers.IO).launch {
                val activeMedicines = repository.getAllActiveForReschedule()
                activeMedicines.forEach { medicine ->
                    val schedules = repository.schedulesForMedicineOnce(medicine.id)
                    if (schedules.isEmpty()) AlarmScheduler.scheduleReminder(context, medicine)
                    else schedules.forEach { AlarmScheduler.scheduleReminder(context, medicine, it) }
                }
            }
        }
    }
}
