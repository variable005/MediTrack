package com.example.meditrack.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.meditrack.R // This import is important

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val MEDICINE_ID_EXTRA = "medicine_id"
        const val MEDICINE_NAME_EXTRA = "medicine_name"
        const val MEDICINE_DOSAGE_EXTRA = "medicine_dosage"
        const val CHANNEL_ID = "medicine_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getIntExtra(MEDICINE_ID_EXTRA, -1)
        val medicineName = intent.getStringExtra(MEDICINE_NAME_EXTRA) ?: "Medicine"
        val medicineDosage = intent.getStringExtra(MEDICINE_DOSAGE_EXTRA) ?: ""

        if (medicineId != -1) {
            showNotification(context, medicineId, medicineName, medicineDosage)
            // TODO: Reschedule for next day
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a real icon
            .setContentTitle("Time for your medicine!")
            .setContentText("Take $name ($dosage)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}