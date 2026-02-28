package com.example.flightsearch.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that mirrors the `airport` table from the pre-packaged database.
 */
@Entity(tableName = "airport")
data class AirportEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "iata_code") val iataCode: String,
    val name: String,
    val passengers: Int,
)

