package com.example.meditrack.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE medicines ADD COLUMN instructions TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE medicines ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE medicines ADD COLUMN imageUri TEXT")
            db.execSQL("ALTER TABLE medicines ADD COLUMN initialQuantity INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE medicines ADD COLUMN remainingQuantity INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE medicines ADD COLUMN refillThreshold INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE medicines ADD COLUMN isPaused INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS medicine_schedules (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, medicineId INTEGER NOT NULL, reminderTime TEXT NOT NULL, daysOfWeek TEXT NOT NULL, intervalDays INTEGER NOT NULL, isEnabled INTEGER NOT NULL, FOREIGN KEY(medicineId) REFERENCES medicines(id) ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_medicine_schedules_medicineId ON medicine_schedules(medicineId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS dose_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, medicineId INTEGER NOT NULL, scheduleId INTEGER, scheduledAt INTEGER NOT NULL, recordedAt INTEGER NOT NULL, status TEXT NOT NULL, note TEXT NOT NULL, FOREIGN KEY(medicineId) REFERENCES medicines(id) ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_dose_logs_medicineId ON dose_logs(medicineId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_dose_logs_medicineId_scheduledAt ON dose_logs(medicineId, scheduledAt)")
        }
    }
}
