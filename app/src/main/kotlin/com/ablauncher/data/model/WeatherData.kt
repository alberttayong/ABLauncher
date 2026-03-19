package com.ablauncher.data.model

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val locationName: String,
    val unit: String = "CELSIUS"
) {
    val displayTemp: String
        get() = if (unit == "CELSIUS") "${temperature.toInt()}°C" else "${toFahrenheit(temperature).toInt()}°F"

    val displayFeelsLike: String
        get() = if (unit == "CELSIUS") "${feelsLike.toInt()}°C" else "${toFahrenheit(feelsLike).toInt()}°F"

    val conditionLabel: String
        get() = weatherCodeToLabel(weatherCode)

    val conditionIcon: String
        get() = weatherCodeToIcon(weatherCode)

    private fun toFahrenheit(c: Double) = c * 9.0 / 5.0 + 32.0
}

fun weatherCodeToLabel(code: Int): String = when (code) {
    0 -> "Clear Sky"
    1, 2, 3 -> "Partly Cloudy"
    45, 48 -> "Foggy"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rainy"
    71, 73, 75 -> "Snowy"
    80, 81, 82 -> "Showers"
    95 -> "Thunderstorm"
    96, 99 -> "Hail Storm"
    else -> "Unknown"
}

fun weatherCodeToIcon(code: Int): String = when (code) {
    0 -> "☀️"
    1, 2, 3 -> "⛅"
    45, 48 -> "🌫️"
    51, 53, 55 -> "🌦️"
    61, 63, 65 -> "🌧️"
    71, 73, 75 -> "❄️"
    80, 81, 82 -> "🌦️"
    95, 96, 99 -> "⛈️"
    else -> "🌡️"
}
