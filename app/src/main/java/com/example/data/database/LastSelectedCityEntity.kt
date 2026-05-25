package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GeocodingResult

@Entity(tableName = "last_selected_city")
data class LastSelectedCityEntity(
    @PrimaryKey val id: Int = 1, // Fix to id 1 to overwrite the single row always
    val cityId: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val countryCode: String? = null,
    val timezone: String? = null,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toGeocodingResult() = GeocodingResult(
        id = cityId,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        admin1 = admin1,
        countryCode = countryCode,
        timezone = timezone
    )

    companion object {
        fun fromGeocodingResult(result: GeocodingResult) = LastSelectedCityEntity(
            id = 1,
            cityId = result.id,
            name = result.name,
            latitude = result.latitude,
            longitude = result.longitude,
            country = result.country,
            admin1 = result.admin1,
            countryCode = result.countryCode,
            timezone = result.timezone
        )
    }
}
