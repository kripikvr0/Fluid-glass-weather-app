package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarked_cities ORDER BY bookmarkedAt DESC")
    fun getBookmarkedCities(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarked_cities WHERE id = :cityId")
    suspend fun deleteBookmark(cityId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_cities WHERE id = :cityId LIMIT 1)")
    fun isCityBookmarkedFlow(cityId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_cities WHERE id = :cityId LIMIT 1)")
    suspend fun isCityBookmarked(cityId: Long): Boolean

    // Last selected city queries
    @Query("SELECT * FROM last_selected_city WHERE id = 1 LIMIT 1")
    suspend fun getLastSelectedCity(): LastSelectedCityEntity?

    @Query("SELECT * FROM last_selected_city WHERE id = 1 LIMIT 1")
    fun getLastSelectedCityFlow(): Flow<LastSelectedCityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLastSelectedCity(city: LastSelectedCityEntity)
}
