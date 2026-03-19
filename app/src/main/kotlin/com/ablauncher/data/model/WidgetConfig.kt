package com.ablauncher.data.model

enum class ClockFace {
    DIGITAL_SIMPLE,
    DIGITAL_FULL,
    ANALOG_MINIMAL,
    ANALOG_CLASSIC;

    val displayName: String get() = when (this) {
        DIGITAL_SIMPLE -> "Digital Simple"
        DIGITAL_FULL -> "Digital Full"
        ANALOG_MINIMAL -> "Analog Minimal"
        ANALOG_CLASSIC -> "Analog Classic"
    }
}

enum class ClockFormat {
    HOUR_12,
    HOUR_24;

    val displayName: String get() = when (this) {
        HOUR_12 -> "12-Hour"
        HOUR_24 -> "24-Hour"
    }
}

enum class WeatherUnit {
    CELSIUS,
    FAHRENHEIT;

    val displayName: String get() = when (this) {
        CELSIUS -> "Celsius (°C)"
        FAHRENHEIT -> "Fahrenheit (°F)"
    }
}
