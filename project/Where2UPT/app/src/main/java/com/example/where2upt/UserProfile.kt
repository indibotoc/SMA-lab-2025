package com.example.where2upt

data class UserProfile(
    val uid: String = "",
    val role: String = "student",
    val facultyId: String = "",
    val displayName: String = "",
    val managedRooms: List<String> = emptyList(), // pentru host
    val email: String = "",
    val photo: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)