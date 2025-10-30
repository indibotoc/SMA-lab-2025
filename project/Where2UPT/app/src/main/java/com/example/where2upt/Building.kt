package com.example.where2upt

data class Building(
    val id: String = "",                   // ex: "CHIM"
    val name: String = "",                 // ex: "Chimie Industrială"
    val address: String = "",
    val status: String = "active",
    val geo: GeoPoint = GeoPoint(),        // locația principală a clădirii
    val blocks: List<Block> = emptyList()  // subclădiri / corpuri
)

data class Block(
    val id: String = "",                   // ex: "A", "CP"
    val name: String = "",                 // ex: "Chimie Parc"
    val geo: GeoPoint = GeoPoint(),
    val status: String = "active"
)

data class GeoPoint(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)