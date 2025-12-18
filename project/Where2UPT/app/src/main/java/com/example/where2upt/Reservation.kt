package com.example.where2upt

import java.util.concurrent.TimeUnit

data class Reservation(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val createdByRole: String = "",
    val startTime: Long = 0L,     // millis UTC
    val endTime: Long = 0L,       // millis UTC
    val purpose: String = "",
    val status: String = "pending", // pending|approved|rejected|cancelled|approved(auto)
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
){
    fun durationHours(): Long = TimeUnit.MILLISECONDS.toHours(endTime - startTime)

    fun isHourAligned(): Boolean {
        val oneHourMs = TimeUnit.HOURS.toMillis(1)
        return startTime % oneHourMs == 0L && endTime % oneHourMs == 0L
    }

    fun isValidHourMultiple(): Boolean {
        val oneHourMs = TimeUnit.HOURS.toMillis(1)
        val dur = endTime - startTime
        return isHourAligned() && dur >= oneHourMs && dur % oneHourMs == 0L
    }

    fun overlaps(other: Reservation): Boolean {
        // [a,b) overlaps [c,d) <=> a < d && b > c
        return this.startTime < other.endTime && this.endTime > other.startTime
    }
}