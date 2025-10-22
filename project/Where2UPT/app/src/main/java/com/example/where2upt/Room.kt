package com.example.where2upt

data class Room(
    val id: String = "",
    val buildingId: String = "",
    val facultyIds: List<String> = emptyList(),   // una sau mai multe
    val floor: Int = 0,
    val roomNumber: String = "",
    val capacity: Int = 0,
    val hasComputers: Boolean = false,
    val pcCount: Int = 0,
    val os: List<String> = emptyList(),
    val apps: List<String> = emptyList(),
    val studentReservable: Boolean = false,
    val status: String = "active",                // active|maintenance|archived
    val hostUid: String = "",
    val geo: Map<String, Double> = emptyMap(),
    val version: Int = 1,
    val updatedAt: Long = 0,
    val lastUpdatedBy: String = ""
)