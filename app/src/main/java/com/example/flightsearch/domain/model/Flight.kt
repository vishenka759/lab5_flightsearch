package com.example.flightsearch.domain.model

data class Flight(
    val id: Long,
    val departure: Airport,
    val destination: Airport,
    val isFavorite: Boolean,
)

