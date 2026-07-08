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
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getIntExtra(MEDICINE_ID_EXTRA, -1)
        val medicineName = intent.getStringExtra(MEDICINE_NAME_EXTRA) ?: "Medicine"
        val medicineDosage = intent.getStringExtra(MEDICINE_DOSAGE_EXTRA) ?: ""
        val repository = (context.applicationContext as MediTrackApplication).repository

        if (intent.action == ACTION_MARK_TAKEN) {
            if (medicineId != -1) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(medicineId)

                CoroutineScope(Dispatchers.IO).launch {
                    val medicine = repository.getMedicineById(medicineId)
                    if (medicine != null) {
                        val updated = medicine.copy(lastTakenTimestamp = System.currentTimeMillis())
                        repository.update(updated)

                        AlarmScheduler.cancelReminder(context, medicine)
                        AlarmScheduler.scheduleReminder(context, updated)

                        MedicineWidgetProvider.updateAllWidgets(context)
                    }
                }
            }
        } else {
            if (medicineId != -1) {
                showNotification(context, medicineId, medicineName, medicineDosage)
                
                CoroutineScope(Dispatchers.IO).launch {
                    val medicine = repository.getMedicineById(medicineId)
                    if (medicine != null) {
                        AlarmScheduler.scheduleReminder(context, medicine)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, id: Int, name: String, dosage: String) {
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
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}