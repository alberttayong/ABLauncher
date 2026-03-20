package com.ablauncher.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class HomeItem {
    abstract val id: String
    abstract val xFrac: Float
    abstract val yFrac: Float

    data class Widget(
        override val id: String = UUID.randomUUID().toString(),
        val widgetType: WidgetType,
        override val xFrac: Float,
        override val yFrac: Float,
        val widthFrac: Float,
        val heightFrac: Float
    ) : HomeItem()

    data class AppShortcut(
        override val id: String = UUID.randomUUID().toString(),
        val packageName: String,
        val appLabel: String = "",
        override val xFrac: Float,
        override val yFrac: Float,
        val iconConfig: IconConfig = IconConfig()
    ) : HomeItem()
}

enum class WidgetType {
    CLOCK, WEATHER, CALENDAR, NEWS, SEARCH;

    val defaultXFrac: Float
        get() = when (this) {
            SEARCH, CLOCK, WEATHER -> 0.02f
            CALENDAR, NEWS -> 0.50f
        }
    val defaultYFrac: Float
        get() = when (this) {
            SEARCH -> 0.03f
            CLOCK -> 0.13f
            WEATHER -> 0.55f
            CALENDAR -> 0.05f
            NEWS -> 0.50f
        }
    val defaultWidthFrac: Float
        get() = when (this) {
            SEARCH, CLOCK, WEATHER -> 0.45f
            CALENDAR, NEWS -> 0.46f
        }
    val defaultHeightFrac: Float
        get() = when (this) {
            SEARCH -> 0.10f
            CLOCK -> 0.38f
            WEATHER -> 0.28f
            CALENDAR -> 0.40f
            NEWS -> 0.44f
        }
}

data class IconConfig(
    val shape: IconShape = IconShape.ROUNDED_SQUARE,
    val style: IconStyle = IconStyle.NORMAL,
    val tintColor: Long? = null,
    val sizeTier: IconSizeTier = IconSizeTier.MEDIUM,
    val showLabel: Boolean = true
)

enum class IconShape {
    CIRCLE, ROUNDED_SQUARE, SQUARE, SQUIRCLE;

    val displayName
        get() = when (this) {
            CIRCLE -> "Circle"
            ROUNDED_SQUARE -> "Rounded"
            SQUARE -> "Square"
            SQUIRCLE -> "Squircle"
        }
}

enum class IconStyle {
    NORMAL, MONO, TINTED, OUTLINED;

    val displayName
        get() = when (this) {
            NORMAL -> "Normal"
            MONO -> "Mono"
            TINTED -> "Tinted"
            OUTLINED -> "Outlined"
        }
}

enum class IconSizeTier(val sizeDp: Int) {
    SMALL(48), MEDIUM(64), LARGE(80), XLARGE(96);

    val displayName
        get() = when (this) {
            SMALL -> "Small"
            MEDIUM -> "Medium"
            LARGE -> "Large"
            XLARGE -> "XL"
        }
}

// ── JSON helpers ──────────────────────────────────────────────────────────────

fun HomeItem.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("xFrac", xFrac.toDouble())
    put("yFrac", yFrac.toDouble())
    when (val item = this@toJson) {
        is HomeItem.Widget -> {
            put("type", "WIDGET")
            put("widgetType", item.widgetType.name)
            put("widthFrac", item.widthFrac.toDouble())
            put("heightFrac", item.heightFrac.toDouble())
        }
        is HomeItem.AppShortcut -> {
            put("type", "APP")
            put("packageName", item.packageName)
            put("appLabel", item.appLabel)
            put("iconShape", item.iconConfig.shape.name)
            put("iconStyle", item.iconConfig.style.name)
            item.iconConfig.tintColor?.let { put("tintColor", it) }
            put("sizeTier", item.iconConfig.sizeTier.name)
            put("showLabel", item.iconConfig.showLabel)
        }
    }
}

fun homeItemFromJson(json: JSONObject): HomeItem? = runCatching {
    val id = json.optString("id", UUID.randomUUID().toString())
    val xFrac = json.optDouble("xFrac", 0.1).toFloat()
    val yFrac = json.optDouble("yFrac", 0.1).toFloat()
    when (json.optString("type")) {
        "WIDGET" -> HomeItem.Widget(
            id = id, xFrac = xFrac, yFrac = yFrac,
            widgetType = WidgetType.valueOf(json.getString("widgetType")),
            widthFrac = json.optDouble("widthFrac", 0.45).toFloat(),
            heightFrac = json.optDouble("heightFrac", 0.30).toFloat()
        )
        else -> HomeItem.AppShortcut(
            id = id, xFrac = xFrac, yFrac = yFrac,
            packageName = json.optString("packageName", ""),
            appLabel = json.optString("appLabel", ""),
            iconConfig = IconConfig(
                shape = runCatching {
                    IconShape.valueOf(json.optString("iconShape", "ROUNDED_SQUARE"))
                }.getOrDefault(IconShape.ROUNDED_SQUARE),
                style = runCatching {
                    IconStyle.valueOf(json.optString("iconStyle", "NORMAL"))
                }.getOrDefault(IconStyle.NORMAL),
                tintColor = if (json.has("tintColor")) json.getLong("tintColor") else null,
                sizeTier = runCatching {
                    IconSizeTier.valueOf(json.optString("sizeTier", "MEDIUM"))
                }.getOrDefault(IconSizeTier.MEDIUM),
                showLabel = json.optBoolean("showLabel", true)
            )
        )
    }
}.getOrNull()

fun List<HomeItem>.toJsonString(): String =
    JSONArray().also { arr -> forEach { arr.put(it.toJson()) } }.toString()

fun homeItemsFromJson(json: String): List<HomeItem> {
    if (json.isBlank()) return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull {
            runCatching { homeItemFromJson(arr.getJSONObject(it)) }.getOrNull()
        }
    } catch (e: Exception) {
        emptyList()
    }
}
