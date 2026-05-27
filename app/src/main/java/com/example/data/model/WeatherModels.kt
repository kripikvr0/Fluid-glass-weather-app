package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @Json(name = "current") val current: CurrentWeather?,
    @Json(name = "hourly") val hourly: HourlyWeather?,
    @Json(name = "daily") val daily: DailyWeather?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Double,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "is_day") val isDay: Int,
    val precipitation: Double,
    val rain: Double,
    val showers: Double,
    val snowfall: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed: Double
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperatures: List<Double>,
    @Json(name = "relative_humidity_2m") val humidities: List<Double>,
    @Json(name = "weather_code") val weatherCodes: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String>,
    @Json(name = "weather_code") val weatherCodes: List<Int>,
    @Json(name = "temperature_2m_max") val temperaturesMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperaturesMin: List<Double>,
    val sunrise: List<String>,
    val sunset: List<String>
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResult>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    @Json(name = "country_code") val countryCode: String? = null,
    val timezone: String? = null
)

@JsonClass(generateAdapter = true)
data class AirQualityResponse(
    @Json(name = "current") val current: CurrentAirQuality?
)

@JsonClass(generateAdapter = true)
data class CurrentAirQuality(
    val time: String,
    @Json(name = "pm2_5") val pm25: Double,
    @Json(name = "pm10") val pm10: Double,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double,
    @Json(name = "sulphur_dioxide") val sulphurDioxide: Double,
    val ozone: Double,
    @Json(name = "us_aqi") val usAqi: Int
)

