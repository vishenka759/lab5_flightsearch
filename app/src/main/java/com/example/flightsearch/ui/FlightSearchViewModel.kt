package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.domain.model.Airport
import com.example.flightsearch.domain.model.Flight
import com.example.flightsearch.domain.repository.FlightRepository
import com.example.flightsearch.domain.repository.UserPreferencesRepository
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlightSearchUiState(
    val query: String = "",
    val suggestions: List<Airport> = emptyList(),
    val flights: List<Flight> = emptyList(),
    val selectedDeparture: Airport? = null,
    val isShowingFavorites: Boolean = true,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isEmptyResult: Boolean = false,
)

class FlightSearchViewModel(
    private val flightRepository: FlightRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightSearchUiState())
    val uiState: StateFlow<FlightSearchUiState> = _uiState.asStateFlow()

    private var suggestionsJob: Job? = null
    private var flightsJob: Job? = null

    init {
        restoreLastSearchOrShowFavorites()
    }

    private fun restoreLastSearchOrShowFavorites() {
        viewModelScope.launch {
            val lastQuery = userPreferencesRepository.lastSearchQuery.first()
            if (lastQuery.isNullOrBlank()) {
                loadFavoriteFlights()
            } else {
                _uiState.update { it.copy(query = lastQuery) }
                observeFlightsForDeparture(lastQuery)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update {
            it.copy(
                query = newQuery,
                isError = false,
                errorMessage = null,
            )
        }

        suggestionsJob?.cancel()

        if (newQuery.isBlank()) {
            _uiState.update {
                it.copy(
                    suggestions = emptyList(),
                    selectedDeparture = null,
                )
            }
            loadFavoriteFlights()
        } else {
            suggestionsJob = viewModelScope.launch {
                flightRepository
                    .searchAirports(newQuery)
                    .catch { throwable ->
                        handleError(throwable, defaultMessage = "Не удалось загрузить список аэропортов")
                    }
                    .collect { airports ->
                        _uiState.update { state ->
                            state.copy(
                                suggestions = airports,
                                isEmptyResult = airports.isEmpty() && state.flights.isEmpty(),
                            )
                        }
                    }
            }
        }
    }

    fun onAirportSelected(airport: Airport) {
        _uiState.update {
            it.copy(
                query = airport.iataCode,
                selectedDeparture = airport,
                suggestions = emptyList(),
                isShowingFavorites = false,
            )
        }

        viewModelScope.launch {
            userPreferencesRepository.saveLastSearchQuery(airport.iataCode)
        }

        observeFlightsForDeparture(airport.iataCode)
    }

    fun onSearchClicked() {
        val query = uiState.value.query.trim()
        if (query.isBlank()) {
            loadFavoriteFlights()
            return
        }


        viewModelScope.launch {
            val airports = flightRepository.searchAirports(query).first()
            val departure =
                airports.firstOrNull { it.iataCode.equals(query, ignoreCase = true) }
                    ?: airports.firstOrNull()

            if (departure != null) {
                onAirportSelected(departure)
            } else {
                _uiState.update {
                    it.copy(
                        flights = emptyList(),
                        isEmptyResult = true,
                    )
                }
            }
        }
    }

    fun onToggleFavorite(flight: Flight) {
        viewModelScope.launch {
            try {
                flightRepository.toggleFavorite(
                    departureCode = flight.departure.iataCode,
                    destinationCode = flight.destination.iataCode,
                )
            } catch (t: Throwable) {
                handleError(t, defaultMessage = "Не удалось обновить избранное")
            }
        }
    }

    private fun observeFlightsForDeparture(departureCode: String) {
        flightsJob?.cancel()

        flightsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isShowingFavorites = false,
                    isError = false,
                    errorMessage = null,
                )
            }

            flightRepository
                .getFlightsFrom(departureCode)
                .catch { throwable ->
                    handleError(throwable, defaultMessage = "Не удалось загрузить рейсы")
                }
                .collect { flights ->
                    _uiState.update {
                        it.copy(
                            flights = flights,
                            isLoading = false,
                            isEmptyResult = flights.isEmpty(),
                        )
                    }
                }
        }
    }

    private fun loadFavoriteFlights() {
        flightsJob?.cancel()

        flightsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isShowingFavorites = true,
                    isError = false,
                    errorMessage = null,
                )
            }

            flightRepository
                .getFavoriteFlights()
                .catch { throwable ->
                    handleError(throwable, defaultMessage = "Не удалось загрузить избранные рейсы")
                }
                .collect { flights ->
                    _uiState.update {
                        it.copy(
                            flights = flights,
                            isLoading = false,
                            isEmptyResult = flights.isEmpty(),
                        )
                    }
                }
        }
    }

    private fun handleError(throwable: Throwable, defaultMessage: String) {
        val message =
            if (throwable is IOException) {
                "$defaultMessage: проблема с чтением БД"
            } else {
                defaultMessage
            }
        _uiState.update {
            it.copy(
                isLoading = false,
                isError = true,
                errorMessage = message,
            )
        }
    }

    companion object {
        fun provideFactory(
            flightRepository: FlightRepository,
            userPreferencesRepository: UserPreferencesRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(FlightSearchViewModel::class.java)) {
                        return FlightSearchViewModel(
                            flightRepository = flightRepository,
                            userPreferencesRepository = userPreferencesRepository,
                        ) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}

