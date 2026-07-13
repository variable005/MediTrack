package com.example.meditrack.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import com.example.meditrack.data.DoseStatus

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }

    @TypeConverter fun fromDoseStatus(status: DoseStatus?): String? = status?.name
    @TypeConverter fun toDoseStatus(value: String?): DoseStatus? = value?.let(DoseStatus::valueOf)
}
