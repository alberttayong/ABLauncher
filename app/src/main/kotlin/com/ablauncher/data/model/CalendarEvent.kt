package com.ablauncher.data.model

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val calendarColor: Int,
    val allDay: Boolean = false
)
