package com.example.flightsearch.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that mirrors the `favorite` table from the pre-packaged database.
 * This table initially contains no rows and is used to store user-selected routes.
 */
@Entity(tableName = "favorite")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "departure_code") val departureCode: String,
    @ColumnInfo(name = "destination_code") val destinationCode: String,
)

