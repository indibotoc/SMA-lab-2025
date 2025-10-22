package com.example.where2upt

data class Reservation(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val createdByRole: String = "",
    val start: Long = 0L,     // millis UTC
    val end: Long = 0L,       // millis UTC
    val purpose: String = "",
    val status: String = "pending", // pending|approved|rejected|cancelled|approved(auto)
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)