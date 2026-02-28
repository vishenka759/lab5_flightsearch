package com.example.flightsearch.domain.repository

import kotlinx.coroutines.flow.Flow


interface UserPreferencesRepository {


    val lastSearchQuery: Flow<String?>


    suspend fun saveLastSearchQuery(iataCode: String)
}

