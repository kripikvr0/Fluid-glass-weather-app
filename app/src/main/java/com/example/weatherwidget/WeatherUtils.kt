package com.example.weatherwidget

object WeatherUtils {
    fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Cerah"
            1, 2, 3 -> "Berawan sebagian"
            45, 48 -> "Berkabut"
            51, 53, 55 -> "Gerimis"
            56, 57 -> "Gerimis beku"
            61, 63, 65 -> "Hujan"
            66, 67 -> "Hujan beku"
            71, 73, 75 -> "Turun salju"
            77 -> "Salju ringan"
            80, 81, 82 -> "Hujan deras"
            85, 86 -> "Salju lebat"
            95 -> "Badai Petir"
            96, 99 -> "Badai & hujan es"
            else -> "Tidak diketahui"
        }
    }

    fun getWeatherIcon(code: Int, isDay: Boolean = true): String {
        return when (code) {
            0 -> if (isDay) "☀️" else "🌙"
            1, 2 -> if (isDay) "🌤️" else "☁️"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌧️" // Drizzle
            61, 63, 65, 66, 67 -> "🌧️" // Rain
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌦️"
            85, 86 -> "🌨️"
            95, 96, 99 -> "⛈️"
            else -> "☁️"
        }
    }
}
