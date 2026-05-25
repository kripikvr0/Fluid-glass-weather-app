package com.example.ui

import androidx.compose.ui.graphics.Color

object WeatherInfoMapper {

    fun getDescription(code: Int): String {
        return when (code) {
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
