package com.example.meditrack.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.RemoteViews
import com.example.meditrack.MainActivity
import com.example.meditrack.MediTrackApplication
import com.example.meditrack.R
import com.example.meditrack.data.Medicine
import com.example.meditrack.reminders.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MedicineWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MARK_TAKEN) {
            val medicineId = intent.getIntExtra(EXTRA_MEDICINE_ID, -1)
            if (medicineId != -1) {
                CoroutineScope(Dispatchers.IO).launch {
                    val repository = (context.applicationContext as MediTrackApplication).repository
                    val medicine = repository.getMedicineById(medicineId)
                    if (medicine != null) {
                        val updated = medicine.copy(lastTakenTimestamp = System.currentTimeMillis())
                        repository.update(updated)

                        // Reschedule alarm notifications
                        AlarmScheduler.cancelReminder(context, medicine)
                        AlarmScheduler.scheduleReminder(context, updated)

                        // Update all widget instances
                        updateAllWidgets(context)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_MARK_TAKEN = "com.example.meditrack.widget.ACTION_MARK_TAKEN"
        const val EXTRA_MEDICINE_ID = "com.example.meditrack.widget.EXTRA_MEDICINE_ID"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MedicineWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            val intent = Intent(context, MedicineWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.medicine_widget_layout)

            // Setup click intent to open main application
            val mainIntent = Intent(context, MainActivity::class.java)
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

            // Dynamic color settings from SharedPreferences
            val sharedPreferences = context.getSharedPreferences("meditrack_settings", Context.MODE_PRIVATE)
            val themeColor = sharedPreferences.getString("theme_color", "teal") ?: "teal"
            val themeColorRes = when (themeColor) {
                "blue" -> R.color.theme_blue
                "purple" -> R.color.theme_purple
                "green" -> R.color.theme_green
                "orange" -> R.color.theme_orange
                else -> R.color.theme_teal
            }
            val colorVal = context.getColor(themeColorRes)
            val colorStateList = ColorStateList.valueOf(colorVal)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                views.setColorStateList(R.id.widget_progress_bar, "setProgressTintList", colorStateList)
                views.setColorStateList(R.id.widget_btn_take, "setBackgroundTintList", colorStateList)
            }
            views.setTextColor(R.id.widget_next_up_header, colorVal)

            // Load data from Room database in background scope
            CoroutineScope(Dispatchers.IO).launch {
                val repository = (context.applicationContext as MediTrackApplication).repository
                val today = LocalDate.now()

                // Fetch active medicines for today
                val activeToday = repository.getAllActiveForReschedule().filter {
                    !today.isBefore(it.startDate) && !today.isAfter(it.endDate)
                }

                val totalDoses = activeToday.size
                val takenDoses = activeToday.count { it.isTakenToday() }
                val progress = if (totalDoses > 0) (takenDoses * 100) / totalDoses else 0

                // UI elements update on Main Dispatcher
                CoroutineScope(Dispatchers.Main).launch {
                    views.setTextViewText(R.id.widget_progress_text, "Taken: $takenDoses / $totalDoses")
                    views.setProgressBar(R.id.widget_progress_bar, 100, progress, false)

                    val remaining = activeToday.filter { !it.isTakenToday() }
                    val nextUp = remaining.sortedBy { it.reminderTime }.firstOrNull()

                    if (nextUp != null) {
                        views.setViewVisibility(R.id.widget_btn_take, View.VISIBLE)
                        views.setTextViewText(R.id.widget_med_name, nextUp.name)

                        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
                        views.setTextViewText(R.id.widget_med_time, "${nextUp.dosage} - ${nextUp.reminderTime.format(timeFormatter)}")

                        // Set up take intent
                        val takeIntent = Intent(context, MedicineWidgetProvider::class.java).apply {
                            action = ACTION_MARK_TAKEN
                            putExtra(EXTRA_MEDICINE_ID, nextUp.id)
                        }
                        val takePendingIntent = PendingIntent.getBroadcast(
                            context,
                            nextUp.id,
                            takeIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_btn_take, takePendingIntent)
                    } else {
                        views.setViewVisibility(R.id.widget_btn_take, View.GONE)
                        if (totalDoses > 0 && takenDoses == totalDoses) {
                            views.setTextViewText(R.id.widget_med_name, "All done for today! 🎉")
                            views.setTextViewText(R.id.widget_med_time, "Nice job keeping consistent.")
                        } else {
                            views.setTextViewText(R.id.widget_med_name, "No medicines scheduled")
                            views.setTextViewText(R.id.widget_med_time, "Have a wonderful day!")
                        }
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}

fun Medicine.isTakenToday(): Boolean {
    if (lastTakenTimestamp == 0L) return false
    val lastTakenDate = Instant.ofEpochMilli(lastTakenTimestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return lastTakenDate == LocalDate.now()
}
