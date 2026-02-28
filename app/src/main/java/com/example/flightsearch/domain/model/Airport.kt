package com.example.flightsearch.domain.model

data class Airport(
    val id: Int,
    val iataCode: String,
    val name: String,
    val passengers: Int,
)

