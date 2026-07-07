package com.example.meditrack.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.meditrack.data.Medicine
import java.time.LocalDate
import java.time.ZoneId

object AlarmScheduler {

    fun scheduleReminder(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.MEDICINE_ID_EXTRA, medicine.id)
            putExtra(AlarmReceiver.MEDICINE_NAME_EXTRA, medicine.name)
            putExtra(AlarmReceiver.MEDICINE_DOSAGE_EXTRA, medicine.dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id, // Use medicine ID as request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reminderTime = medicine.reminderTime
        val now = LocalDate.now()

        if (medicine.endDate.isBefore(now)) {
            return
        }

        var nextReminderDateTime = now.atTime(reminderTime)

        if (nextReminderDateTime.isBefore(java.time.LocalDateTime.now())) {
            nextReminderDateTime = nextReminderDateTime.plusDays(1)
        }

        if (nextReminderDateTime.toLocalDate().isAfter(medicine.endDate)) {
            return
        }

        val triggerAtMillis = nextReminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(context: Context, medicine: Medicine) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicine.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}