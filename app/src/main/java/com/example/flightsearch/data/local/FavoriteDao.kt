package com.example.flightsearch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite WHERE departure_code = :departureCode")
    fun getFavoritesForDeparture(departureCode: String): Flow<List<FavoriteEntity>>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM favorite 
            WHERE departure_code = :departureCode 
              AND destination_code = :destinationCode
        )
        """
    )
    suspend fun isFavorite(departureCode: String, destinationCode: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query(
        """
        DELETE FROM favorite 
        WHERE departure_code = :departureCode 
          AND destination_code = :destinationCode
        """
    )
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)
}

