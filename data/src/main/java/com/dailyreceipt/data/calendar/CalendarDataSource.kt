package com.dailyreceipt.data.calendar

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.dailyreceipt.domain.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource for calendar events using CalendarContract.
 * Requires READ_CALENDAR permission.
 */
@Singleton
class CalendarDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Check if calendar permission is granted.
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get calendar events for a specific date range.
     */
    suspend fun getEventsForDateRange(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            return@withContext emptyList()
        }

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.CALENDAR_ID
        )

        val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        val events = mutableListOf<CalendarEvent>()

        try {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    events.add(cursor.toCalendarEvent())
                }
            }
        } catch (e: SecurityException) {
            // Permission was revoked
        }

        events
    }

    /**
     * Get calendar events for a specific day.
     */
    suspend fun getEventsForDay(date: LocalDateTime): List<CalendarEvent> {
        val startOfDay = date.toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return getEventsForDateRange(startOfDay, endOfDay)
    }

    /**
     * Get all calendars available on the device.
     */
    suspend fun getAvailableCalendars(): List<CalendarInfo> = withContext(Dispatchers.IO) {
        if (!hasPermission()) {
            return@withContext emptyList()
        }

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.IS_PRIMARY
        )

        val calendars = mutableListOf<CalendarInfo>()

        try {
            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    calendars.add(
                        CalendarInfo(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)),
                            displayName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)),
                            accountName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)),
                            accountType = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)),
                            isPrimary = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)) == 1
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission was revoked
        }

        calendars
    }

    private fun Cursor.toCalendarEvent(): CalendarEvent {
        val id = getLong(getColumnIndexOrThrow(CalendarContract.Events._ID))
        val title = getString(getColumnIndexOrThrow(CalendarContract.Events.TITLE)) ?: "No Title"
        val description = getString(getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
        val location = getString(getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
        val dtStart = getLong(getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
        val dtEnd = getLongOrNull(getColumnIndexOrThrow(CalendarContract.Events.DTEND))
        val allDay = getInt(getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)) == 1
        val calendarId = getLong(getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID))

        val startTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(dtStart),
            ZoneId.systemDefault()
        )

        val endTime = dtEnd?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        }

        return CalendarEvent(
            id = id,
            title = title,
            description = description,
            location = location,
            startTime = startTime,
            endTime = endTime,
            isAllDay = allDay,
            calendarId = calendarId
        )
    }

    private fun Cursor.getLongOrNull(columnIndex: Int): Long? {
        return if (isNull(columnIndex)) null else getLong(columnIndex)
    }
}

data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val accountType: String,
    val isPrimary: Boolean
)
