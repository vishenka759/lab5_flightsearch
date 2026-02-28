package com.example.flightsearch.data.repository

import com.example.flightsearch.data.local.AirportDao
import com.example.flightsearch.data.local.AirportEntity
import com.example.flightsearch.data.local.FavoriteDao
import com.example.flightsearch.data.local.FavoriteEntity
import com.example.flightsearch.domain.model.Airport
import com.example.flightsearch.domain.model.Flight
import com.example.flightsearch.domain.repository.FlightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Offline implementation of [FlightRepository] that works purely with the local Room database.
 */
class OfflineFlightRepository(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao,
) : FlightRepository {

    override fun searchAirports(query: String): Flow<List<Airport>> =
        if (query.isBlank()) {
            // When query is empty we return the most popular airports.
            airportDao.getAllAirports().map { it.map(AirportEntity::toDomain) }
        } else {
            airportDao.searchAirports(query.trim()).map { it.map(AirportEntity::toDomain) }
        }

    override fun getFlightsFrom(departureCode: String): Flow<List<Flight>> {
        val trimmedCode = departureCode.trim().uppercase()

        // Combine all airports with favorites for the given departure airport.
        return combine(
            airportDao.getAllAirports(),
            favoriteDao.getFavoritesForDeparture(trimmedCode),
        ) { airports, favoritesForDeparture ->
            val departureAirportEntity =
                airports.firstOrNull { it.iataCode.equals(trimmedCode, ignoreCase = true) }
                    ?: return@combine emptyList()

            val favoriteDestinationCodes =
                favoritesForDeparture.map { it.destinationCode.uppercase() }.toSet()

            val departureAirport = departureAirportEntity.toDomain()

            airports
                .filter { !it.iataCode.equals(trimmedCode, ignoreCase = true) }
                .mapIndexed { index, destinationEntity ->
                    Flight(
                        id = index.toLong(),
                        departure = departureAirport,
                        destination = destinationEntity.toDomain(),
                        isFavorite = favoriteDestinationCodes.contains(
                            destinationEntity.iataCode.uppercase(),
                        ),
                    )
                }
        }
    }

    override fun getFavoriteFlights(): Flow<List<Flight>> =
        combine(
            favoriteDao.getAllFavorites(),
            airportDao.getAllAirports(),
        ) { favorites, airports ->
            if (favorites.isEmpty() || airports.isEmpty()) return@combine emptyList()

            val airportsByCode = airports.associateBy { it.iataCode.uppercase() }

            favorites.mapNotNull { favorite ->
                val departure =
                    airportsByCode[favorite.departureCode.uppercase()] ?: return@mapNotNull null
                val destination =
                    airportsByCode[favorite.destinationCode.uppercase()] ?: return@mapNotNull null

                Flight(
                    id = favorite.id.toLong(),
                    departure = departure.toDomain(),
                    destination = destination.toDomain(),
                    isFavorite = true,
                )
            }
        }

    override suspend fun toggleFavorite(departureCode: String, destinationCode: String) {
        val dep = departureCode.trim().uppercase()
        val dest = destinationCode.trim().uppercase()

        val isFavorite = favoriteDao.isFavorite(dep, dest)
        if (isFavorite) {
            favoriteDao.deleteFavorite(dep, dest)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    departureCode = dep,
                    destinationCode = dest,
                ),
            )
        }
    }
}

private fun AirportEntity.toDomain(): Airport =
    Airport(
        id = id,
        iataCode = iataCode,
        name = name,
        passengers = passengers,
    )

