package com.example.meditrack.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.meditrack.data.Medicine
import com.example.meditrack.data.MedicineSchedule
import java.time.LocalDate
import java.time.ZoneId

object AlarmScheduler {

    fun scheduleReminder(context: Context, medicine: Medicine, schedule: MedicineSchedule? = null, snoozeMinutes: Long = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.MEDICINE_ID_EXTRA, medicine.id)
            putExtra(AlarmReceiver.MEDICINE_NAME_EXTRA, medicine.name)
            putExtra(AlarmReceiver.MEDICINE_DOSAGE_EXTRA, medicine.dosage)
            putExtra(AlarmReceiver.SCHEDULE_ID_EXTRA, schedule?.id ?: 0)
            putExtra(AlarmReceiver.SCHEDULED_AT_EXTRA, System.currentTimeMillis() + snoozeMinutes * 60_000)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(medicine.id, schedule?.id ?: 0),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (medicine.isPaused || schedule?.isEnabled == false) return
        val reminderTime = schedule?.reminderTime ?: medicine.reminderTime
        val now = LocalDate.now()

        if (medicine.endDate.isBefore(now)) {
            return
        }

        var nextReminderDateTime = if (snoozeMinutes > 0) java.time.LocalDateTime.now().plusMinutes(snoozeMinutes) else now.atTime(reminderTime)

        if (nextReminderDateTime.isBefore(java.time.LocalDateTime.now())) {
            nextReminderDateTime = nextReminderDateTime.plusDays(1)
        }

        while (!matchesSchedule(nextReminderDateTime.toLocalDate(), medicine.startDate, schedule)) nextReminderDateTime = nextReminderDateTime.plusDays(1)
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

    fun cancelReminder(context: Context, medicine: Medicine, schedule: MedicineSchedule? = null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(medicine.id, schedule?.id ?: 0),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun requestCode(medicineId: Int, scheduleId: Int) = medicineId * 10_000 + scheduleId

    private fun matchesSchedule(date: LocalDate, start: LocalDate, schedule: MedicineSchedule?): Boolean {
        if (schedule == null) return true
        if (schedule.intervalDays > 1 && java.time.temporal.ChronoUnit.DAYS.between(start, date) % schedule.intervalDays != 0L) return false
        return schedule.daysOfWeek.split(',').mapNotNull { it.toIntOrNull() }.contains(date.dayOfWeek.value)
    }
}
