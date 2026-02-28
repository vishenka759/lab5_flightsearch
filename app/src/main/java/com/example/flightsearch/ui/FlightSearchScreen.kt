package com.example.flightsearch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.flightsearch.domain.model.Airport
import com.example.flightsearch.domain.model.Flight

@Composable
fun FlightSearchRoute(
    viewModel: FlightSearchViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    FlightSearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onAirportSelected = viewModel::onAirportSelected,
        onSearchClicked = viewModel::onSearchClicked,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier,
    )
}

@Composable
fun FlightSearchScreen(
    state: FlightSearchUiState,
    onQueryChange: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    onSearchClicked: () -> Unit,
    onToggleFavorite: (Flight) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "Поиск рейсов",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchSection(
                query = state.query,
                suggestions = state.suggestions,
                onQueryChange = onQueryChange,
                onAirportSelected = onAirportSelected,
                onSearchClicked = onSearchClicked,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.isError && state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (state.isEmptyResult) {
                Text(
                    text = if (state.isShowingFavorites) {
                        "Список избранных рейсов пуст"
                    } else {
                        "По вашему запросу рейсы не найдены"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlightsList(
                flights = state.flights,
                isShowingFavorites = state.isShowingFavorites,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    suggestions: List<Airport>,
    onQueryChange: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    onSearchClicked: () -> Unit,
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Код IATA или название аэропорта") },
            singleLine = true,
        )

        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                ) {
                    items(suggestions) { airport ->
                        SuggestionItem(
                            airport = airport,
                            onClick = { onAirportSelected(airport) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSearchClicked,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Найти")
        }
    }
}

@Composable
private fun SuggestionItem(
    airport: Airport,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = airport.iataCode,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = airport.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightsList(
    flights: List<Flight>,
    isShowingFavorites: Boolean,
    onToggleFavorite: (Flight) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val title =
            if (isShowingFavorites) {
                "Избранные рейсы"
            } else {
                "Рейсы"
            }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(flights) { flight ->
                FlightItem(
                    flight = flight,
                    onToggleFavorite = { onToggleFavorite(flight) },
                )
            }
        }
    }
}

@Composable
private fun FlightItem(
    flight: Flight,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "${flight.departure.iataCode} → ${flight.destination.iataCode}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = flight.destination.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "ID рейса: ${flight.id}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Icon(
                imageVector = if (flight.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (flight.isFavorite) {
                    "Удалить из избранного"
                } else {
                    "Добавить в избранное"
                },
                tint = if (flight.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(onClick = onToggleFavorite),
            )
        }
    }
}

