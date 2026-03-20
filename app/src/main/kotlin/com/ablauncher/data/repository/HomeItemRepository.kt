package com.ablauncher.data.repository

import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.WidgetType
import com.ablauncher.data.model.homeItemsFromJson
import com.ablauncher.data.model.toJsonString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeItemRepository @Inject constructor(
    private val prefs: PreferencesDataStore
) {
    val homeItems: Flow<List<HomeItem>> = prefs.homeItemsJson
        .map { homeItemsFromJson(it) }

    suspend fun save(items: List<HomeItem>) {
        prefs.setHomeItemsJson(items.toJsonString())
    }

    suspend fun initIfEmpty() {
        if (prefs.homeItemsJson.first().isBlank()) {
            save(defaultItems())
        }
    }

    private fun defaultItems(): List<HomeItem> = listOf(
        HomeItem.Widget(
            widgetType = WidgetType.SEARCH,
            xFrac = WidgetType.SEARCH.defaultXFrac,
            yFrac = WidgetType.SEARCH.defaultYFrac,
            widthFrac = WidgetType.SEARCH.defaultWidthFrac,
            heightFrac = WidgetType.SEARCH.defaultHeightFrac
        ),
        HomeItem.Widget(
            widgetType = WidgetType.CLOCK,
            xFrac = WidgetType.CLOCK.defaultXFrac,
            yFrac = WidgetType.CLOCK.defaultYFrac,
            widthFrac = WidgetType.CLOCK.defaultWidthFrac,
            heightFrac = WidgetType.CLOCK.defaultHeightFrac
        ),
        HomeItem.Widget(
            widgetType = WidgetType.WEATHER,
            xFrac = WidgetType.WEATHER.defaultXFrac,
            yFrac = WidgetType.WEATHER.defaultYFrac,
            widthFrac = WidgetType.WEATHER.defaultWidthFrac,
            heightFrac = WidgetType.WEATHER.defaultHeightFrac
        ),
        HomeItem.Widget(
            widgetType = WidgetType.CALENDAR,
            xFrac = WidgetType.CALENDAR.defaultXFrac,
            yFrac = WidgetType.CALENDAR.defaultYFrac,
            widthFrac = WidgetType.CALENDAR.defaultWidthFrac,
            heightFrac = WidgetType.CALENDAR.defaultHeightFrac
        )
    )
}
