package com.example.flightsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.flightsearch.ui.theme.FlightSearchTheme
import com.example.flightsearch.ui.FlightSearchRoute
import com.example.flightsearch.ui.FlightSearchViewModel

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer
        get() = (application as FlightSearchApplication).container

    private val viewModel: FlightSearchViewModel by viewModels {
        FlightSearchViewModel.Companion.provideFactory(
            flightRepository = appContainer.flightRepository,
            userPreferencesRepository = appContainer.userPreferencesRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlightSearchTheme {
                FlightSearchRoute(viewModel = viewModel)
            }
        }
    }
}