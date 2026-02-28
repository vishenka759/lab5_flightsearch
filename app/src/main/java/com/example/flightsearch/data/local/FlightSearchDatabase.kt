package com.example.flightsearch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AirportEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FlightSearchDatabase : RoomDatabase() {

    abstract fun airportDao(): AirportDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var Instance: FlightSearchDatabase? = null

        fun getDatabase(context: Context): FlightSearchDatabase =
            Instance ?: synchronized(this) {
                Instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FlightSearchDatabase::class.java,
                    "flight_search.db",
                )
                    // Pre-populate from the bundled asset database.
                    .createFromAsset("flight_search.db")
                    .build()
                    .also { Instance = it }
            }
    }
}

