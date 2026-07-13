package com.example.meditrack.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.meditrack.MainActivity
import com.example.meditrack.MediTrackApplication
import com.example.meditrack.R
import com.example.meditrack.widget.MedicineWidgetProvider
import com.example.meditrack.data.DoseLog
import com.example.meditrack.data.DoseStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val MEDICINE_ID_EXTRA = "medicine_id"
        const val MEDICINE_NAME_EXTRA = "medicine_name"
        const val MEDICINE_DOSAGE_EXTRA = "medicine_dosage"
        const val CHANNEL_ID = "medicine_reminder_channel"
        const val ACTION_MARK_TAKEN = "com.example.meditrack.reminders.ACTION_MARK_TAKEN"
        const val ACTION_SKIP = "com.example.meditrack.reminders.ACTION_SKIP"
        const val ACTION_SNOOZE = "com.example.meditrack.reminders.ACTION_SNOOZE"
        const val SCHEDULE_ID_EXTRA = "schedule_id"
        const val SCHEDULED_AT_EXTRA = "scheduled_at"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getIntExtra(MEDICINE_ID_EXTRA, -1)
        val medicineName = intent.getStringExtra(MEDICINE_NAME_EXTRA) ?: "Medicine"
        val medicineDosage = intent.getStringExtra(MEDICINE_DOSAGE_EXTRA) ?: ""
        val scheduleId = intent.getIntExtra(SCHEDULE_ID_EXTRA, 0).takeIf { it != 0 }
        val scheduledAt = intent.getLongExtra(SCHEDULED_AT_EXTRA, System.currentTimeMillis())
        val repository = (context.applicationContext as MediTrackApplication).repository

        if (intent.action == ACTION_MARK_TAKEN || intent.action == ACTION_SKIP) {
            if (medicineId != -1) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(medicineId)

                CoroutineScope(Dispatchers.IO).launch {
                    val medicine = repository.getMedicineById(medicineId)
                    if (medicine != null) {
                        val taken = intent.action == ACTION_MARK_TAKEN
                        val updated = if (taken) medicine.copy(lastTakenTimestamp = System.currentTimeMillis(), remainingQuantity = (medicine.remainingQuantity - 1).coerceAtLeast(0)) else medicine
                        repository.update(updated)
                        repository.logDose(DoseLog(medicineId = medicine.id, scheduleId = scheduleId, scheduledAt = scheduledAt, status = if (taken) DoseStatus.TAKEN else DoseStatus.SKIPPED))

                        val schedule = scheduleId?.let { id -> repository.schedulesForMedicineOnce(medicine.id).firstOrNull { it.id == id } }
                        AlarmScheduler.cancelReminder(context, medicine, schedule)
                        AlarmScheduler.scheduleReminder(context, updated, schedule)

                        MedicineWidgetProvider.updateAllWidgets(context)
                    }
                }
            }
        } else if (intent.action == ACTION_SNOOZE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(medicineId)

            CoroutineScope(Dispatchers.IO).launch {
                repository.getMedicineById(medicineId)?.let { medicine ->
                    val schedule = scheduleId?.let { id -> repository.schedulesForMedicineOnce(medicine.id).firstOrNull { it.id == id } }
                    AlarmScheduler.scheduleReminder(context, medicine, schedule, snoozeMinutes = 10)
                }
            }
        } else {
            if (medicineId != -1) {
                showNotification(context, medicineId, medicineName, medicineDosage, scheduleId, scheduledAt)
                
                CoroutineScope(Dispatchers.IO).launch {
                    val medicine = repository.getMedicineById(medicineId)
                    if (medicine != null) {
                        val schedule = scheduleId?.let { id -> repository.schedulesForMedicineOnce(medicine.id).firstOrNull { it.id == id } }
                        AlarmScheduler.scheduleReminder(context, medicine, schedule)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, id: Int, name: String, dosage: String, scheduleId: Int?, scheduledAt: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for medicine reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap intent to open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action intent to mark as taken directly from notification
        val markTakenIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(MEDICINE_ID_EXTRA, id)
            putExtra(SCHEDULE_ID_EXTRA, scheduleId ?: 0)
            putExtra(SCHEDULED_AT_EXTRA, scheduledAt)
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        fun actionPending(action: String, codeOffset: Int): PendingIntent = PendingIntent.getBroadcast(context, id * 10 + codeOffset, Intent(context, AlarmReceiver::class.java).apply {
            this.action = action; putExtra(MEDICINE_ID_EXTRA, id); putExtra(SCHEDULE_ID_EXTRA, scheduleId ?: 0); putExtra(SCHEDULED_AT_EXTRA, scheduledAt)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a real icon
            .setContentTitle("Time for your medicine!")
            .setContentText("Take $name ($dosage)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Mark as Taken",
                markTakenPendingIntent
            )
            .addAction(R.drawable.ic_launcher_foreground, "Snooze 10 min", actionPending(ACTION_SNOOZE, 2))
            .addAction(R.drawable.ic_launcher_foreground, "Skip", actionPending(ACTION_SKIP, 3))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
