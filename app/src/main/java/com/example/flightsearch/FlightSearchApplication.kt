package com.example.flightsearch

import android.app.Application
import android.content.Context
import com.example.flightsearch.data.local.FlightSearchDatabase
import com.example.flightsearch.data.preferences.userPreferencesDataStore
import com.example.flightsearch.data.preferences.UserPreferencesRepositoryImpl
import com.example.flightsearch.data.repository.OfflineFlightRepository
import com.example.flightsearch.domain.repository.FlightRepository
import com.example.flightsearch.domain.repository.UserPreferencesRepository

interface AppContainer {
    val flightRepository: FlightRepository
    val userPreferencesRepository: UserPreferencesRepository
}

private class DefaultAppContainer(context: Context) : AppContainer {

    private val database = FlightSearchDatabase.getDatabase(context)
    private val dataStore = context.userPreferencesDataStore

    override val flightRepository: FlightRepository =
        OfflineFlightRepository(
            airportDao = database.airportDao(),
            favoriteDao = database.favoriteDao(),
        )

    override val userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepositoryImpl(dataStore)
}

class FlightSearchApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

