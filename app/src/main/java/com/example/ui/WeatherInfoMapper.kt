package com.example.ui

import androidx.compose.ui.graphics.Color

object WeatherInfoMapper {

    fun getDescription(code: Int, language: String = "ID"): String {
        val isEng = language == "EN"
        return if (isEng) {
            when (code) {
                0 -> "Clear Sky"
                1 -> "Mainly Clear"
                2 -> "Partly Cloudy"
                3 -> "Overcast"
                45 -> "Foggy"
                48 -> "Depositing Rime Fog"
                51 -> "Light Drizzle"
                53 -> "Moderate Drizzle"
                55 -> "Dense Drizzle"
                56 -> "Light Freezing Drizzle"
                57 -> "Dense Freezing Drizzle"
                61 -> "Slight Rain"
                63 -> "Moderate Rain"
                65 -> "Heavy Rain"
                66 -> "Light Freezing Rain"
                67 -> "Heavy Freezing Rain"
                71 -> "Slight Snow"
                73 -> "Moderate Snow"
                75 -> "Heavy Snow"
                77 -> "Snow Grains"
                80 -> "Slight Rain Showers"
                81 -> "Moderate Rain Showers"
                82 -> "Violent Rain Showers"
                85 -> "Slight Snow Showers"
                86 -> "Heavy Snow Showers"
                95 -> "Thunderstorm"
                96 -> "Thunderstorm with Slight Hail"
                99 -> "Thunderstorm with Heavy Hail"
                else -> "Cloudy"
            }
        } else {
            when (code) {
                0 -> "Cerah"
                1 -> "Cerah Berawan"
                2 -> "Berawan Sebagian"
                3 -> "Mendung"
                45 -> "Berkabut"
                48 -> "Kabut Rime"
                51 -> "Gerimis Ringan"
                53 -> "Gerimis Sedang"
                55 -> "Gerimis Lebat"
                56 -> "Gerimis Beku Ringan"
                57 -> "Gerimis Beku Lebat"
                61 -> "Hujan Ringan"
                63 -> "Hujan Sedang"
                65 -> "Hujan Lebat"
                66 -> "Hujan Es Ringan"
                67 -> "Hujan Es"
                71 -> "Salju Ringan"
                73 -> "Salju Sedang"
                75 -> "Salju Lebat"
                77 -> "Butiran Salju"
                80 -> "Hujan Deras Ringan"
                81 -> "Hujan Deras"
                82 -> "Hujan Deras Sangat Lebat"
                85 -> "Hujan Salju Ringan"
                86 -> "Hujan Salju Lebat"
                95 -> "Badai Petir"
                96 -> "Badai Petir dengan Hail Ringan"
                99 -> "Badai Petir dengan Hail Sangat Lebat"
                else -> "Awan Syahdu"
            }
        }
    }

    fun getCategory(code: Int): String {
        return when (code) {
            0 -> "Cerah"
            1, 2, 3 -> "Berawan"
            45, 48 -> "Berkabut"
            51, 53, 55, 56, 57 -> "Gerimis"
            61, 63, 65, 66, 67, 80, 81, 82 -> "Hujan"
            71, 73, 75, 77, 85, 86 -> "Salju"
            95, 96, 99 -> "Badai"
            else -> "Berawan"
        }
    }
}
