package com.example.data.model

data class CityWeather(
    val cityName: String,
    val temp: Double,
    val condition: String,
    val iconEmoji: String,
    val windSpeed: Double, // km/h
    val rainfall: Double, // mm
    val humidity: Int, // %
    val aqi: Int, // Air Quality Index
    val pm25: Double,
    val uvIndex: Double,
    val hourlyForecast: List<ForecastHour>,
    val dailyForecast: List<ForecastDay>,
    val timestamp: Long = System.currentTimeMillis()
)

data class ForecastHour(
    val time: String, // e.g., "14:00"
    val temp: Double,
    val iconEmoji: String
)

data class ForecastDay(
    val dayName: String, // e.g., "Besok", "Kamis", etc.
    val dateLabel: String, // e.g., "29 Mei"
    val tempMin: Double,
    val tempMax: Double,
    val condition: String,
    val iconEmoji: String
)
