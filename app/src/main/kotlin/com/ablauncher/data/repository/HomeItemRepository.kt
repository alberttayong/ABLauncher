package com.ablauncher.data.repository

import com.ablauncher.data.datastore.PreferencesDataStore
import com.ablauncher.data.model.HomeItem
import com.ablauncher.data.model.WidgetType
import com.ablauncher.data.model.homeItemsFromJson
import com.ablauncher.data.model.toJsonString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeItemRepository @Inject constructor(
    private val prefs: PreferencesDataStore
) {
    // ── Multi-page flow ───────────────────────────────────────────────────────
    val homePages: Flow<List<List<HomeItem>>> = prefs.homePagesJson
        .map { parsePagesJson(it) }

    // Backward compat: flat list across all pages (for widget-enabled checks)
    val homeItems: Flow<List<HomeItem>> = homePages.map { pages -> pages.flatten() }

    // ── Persistence ───────────────────────────────────────────────────────────
    suspend fun savePages(pages: List<List<HomeItem>>) {
        prefs.setHomePagesJson(pagesToJsonString(pages))
    }

    /** Legacy single-page save — mutates page 0 only. */
    suspend fun save(items: List<HomeItem>) {
        val pages = homePages.first().toMutableList()
        when {
            pages.isEmpty() -> savePages(listOf(items))
            else -> { pages[0] = items; savePages(pages) }
        }
    }

    // ── Init / migration ──────────────────────────────────────────────────────
    suspend fun initIfEmpty() {
        val pagesJson = prefs.homePagesJson.first()
        if (pagesJson.isBlank()) {
            val legacy = prefs.homeItemsJson.first()
            if (legacy.isNotBlank()) {
                savePages(listOf(homeItemsFromJson(legacy)))
            } else {
                savePages(listOf(defaultItems()))
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun pagesToJsonString(pages: List<List<HomeItem>>): String {
        val outer = JSONArray()
        pages.forEach { page -> outer.put(JSONArray(page.toJsonString())) }
        return outer.toString()
    }

    private fun parsePagesJson(json: String): List<List<HomeItem>> {
        if (json.isBlank()) return emptyList()
        return try {
            val outer = JSONArray(json)
            (0 until outer.length()).map { i ->
                homeItemsFromJson(outer.optJSONArray(i)?.toString() ?: "[]")
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun defaultItems(): List<HomeItem> = listOf(
        HomeItem.Widget(
            widgetType = WidgetType.SEARCH,
            xFrac = WidgetType.SEARCH.defaultXFrac, yFrac = WidgetType.SEARCH.defaultYFrac,
            widthFrac = WidgetType.SEARCH.defaultWidthFrac, heightFrac = WidgetType.SEARCH.defaultHeightFrac
        ),
        HomeItem.Widget(
            widgetType = WidgetType.CLOCK,
            xFrac = WidgetType.CLOCK.defaultXFrac, yFrac = WidgetType.CLOCK.defaultYFrac,
            widthFrac = WidgetType.CLOCK.defaultWidthFrac, heightFrac = WidgetType.CLOCK.defaultHeightFrac
        ),
        HomeItem.Widget(
            widgetType = WidgetType.WEATHER,
            xFrac = WidgetType.WEATHER.defaultXFrac, yFrac = WidgetType.WEATHER.defaultYFrac,
            widthFrac = WidgetType.WEATHER.defaultWidthFrac, heightFrac = WidgetType.WEATHER.defaultHeightFrac
        ),
        HomeItem.Widget(
            widgetType = WidgetType.CALENDAR,
            xFrac = WidgetType.CALENDAR.defaultXFrac, yFrac = WidgetType.CALENDAR.defaultYFrac,
            widthFrac = WidgetType.CALENDAR.defaultWidthFrac, heightFrac = WidgetType.CALENDAR.defaultHeightFrac
        )
    )
}
