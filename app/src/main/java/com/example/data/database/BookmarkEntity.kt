package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GeocodingResult

@Entity(tableName = "bookmarked_cities")
data class BookmarkEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null,
    val countryCode: String? = null,
    val timezone: String? = null,
    val bookmarkedAt: Long = System.currentTimeMillis()
) {
    fun toGeocodingResult() = GeocodingResult(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        admin1 = admin1,
        countryCode = countryCode,
        timezone = timezone
    )

    companion object {
        fun fromGeocodingResult(result: GeocodingResult) = BookmarkEntity(
            id = result.id,
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
