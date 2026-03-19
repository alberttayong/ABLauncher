package com.ablauncher.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import com.ablauncher.data.model.CalendarEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getUpcomingEvents(limit: Int = 5): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<CalendarEvent>()
        val now = System.currentTimeMillis()
        val end = now + 7L * 24 * 60 * 60 * 1000 // next 7 days

        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_COLOR,
            CalendarContract.Events.ALL_DAY
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), end.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC LIMIT $limit"

        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                while (cursor.moveToNext()) {
                    events.add(
                        CalendarEvent(
                            id = cursor.getLong(0),
                            title = cursor.getString(1) ?: "",
                            startTime = cursor.getLong(2),
                            endTime = cursor.getLong(3),
                            calendarColor = cursor.getInt(4),
                            allDay = cursor.getInt(5) == 1
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        } catch (e: Exception) {
            // Ignore other errors
        }

        events
    }
}
