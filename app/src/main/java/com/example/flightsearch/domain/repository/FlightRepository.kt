package com.example.flightsearch.domain.repository

import com.example.flightsearch.domain.model.Airport
import com.example.flightsearch.domain.model.Flight
import kotlinx.coroutines.flow.Flow


interface FlightRepository {


    fun searchAirports(query: String): Flow<List<Airport>>


    fun getFlightsFrom(departureCode: String): Flow<List<Flight>>


    fun getFavoriteFlights(): Flow<List<Flight>>


    suspend fun toggleFavorite(departureCode: String, destinationCode: String)
}

