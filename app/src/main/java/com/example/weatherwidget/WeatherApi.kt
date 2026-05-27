package com.example.weatherwidget

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Open-Meteo response models
@Serializable
data class OpenMeteoResponse(
    val current: CurrentWeather? = null,
    val hourly: HourlyWeather? = null,
    val daily: DailyWeather? = null
)

@Serializable
data class CurrentWeather(
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val is_day: Int,
    val precipitation: Double,
    val weather_code: Int,
    val wind_speed_10m: Double
)

@Serializable
data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

@Serializable
data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val sunset: List<String>,
    val sunrise: List<String>
)

object WeatherApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            })
        }
    }

    suspend fun getJakartaWeather(): OpenMeteoResponse? {
        return try {
            kotlinx.coroutines.withTimeoutOrNull(5000) {
                client.get("https://api.open-meteo.com/v1/forecast?latitude=-6.2088&longitude=106.8456&current=temperature_2m,relative_humidity_2m,is_day,precipitation,weather_code,wind_speed_10m&hourly=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,sunset,sunrise&timezone=Asia%2FJakarta&forecast_days=1").body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
