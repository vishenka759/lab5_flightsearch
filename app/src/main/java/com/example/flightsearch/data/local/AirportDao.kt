package com.example.flightsearch.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query(
        """
        SELECT * FROM airport
        WHERE iata_code LIKE :query || '%' 
           OR name LIKE '%' || :query || '%'
        ORDER BY passengers DESC
        """
    )
    fun searchAirports(query: String): Flow<List<AirportEntity>>

    @Query("SELECT * FROM airport ORDER BY passengers DESC")
    fun getAllAirports(): Flow<List<AirportEntity>>

    @Query("SELECT * FROM airport WHERE iata_code = :code LIMIT 1")
    suspend fun getAirportByCode(code: String): AirportEntity?
}

