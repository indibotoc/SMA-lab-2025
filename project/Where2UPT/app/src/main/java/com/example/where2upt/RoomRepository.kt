package com.example.where2upt

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class RoomRepository {
    private val col = FirebaseFirestore.getInstance()

    // Căutare „specific room”: ignoră câmpurile goale automat
    suspend fun searchSpecific(
        buildingId: String?, blockId: String?, floor: Int?, roomNumber: String?
    ): List<Room> {
        var q: Query = col.collection("rooms")
        buildingId.blankToNull()?.let { q = q.whereEqualTo("buildingId", it) }
        blockId.blankToNull()?.let { q = q.whereEqualTo("blockId", it) }
        floor?.let { q = q.whereEqualTo("floor", it) }
        roomNumber.blankToNull()?.let { q = q.whereEqualTo("roomNumber", it) }
        return q.get().await().toObjects(Room::class.java)
    }

    // Căutare după preferințe: ignora campurile null
    suspend fun searchByPreferences(
        minCapacity: Int?, hasComputers: Boolean?, os: String?,
        buildingId: String? = null, blockId: String? = null
    ): List<Room> {
        var q: Query = col.collection("rooms")
        buildingId.blankToNull()?.let { q = q.whereEqualTo("buildingId", it) }
        blockId.blankToNull()?.let { q = q.whereEqualTo("blockId", it) }
        minCapacity?.let { q = q.whereGreaterThanOrEqualTo("capacity", it) }
        hasComputers?.let { q = q.whereEqualTo("hasComputers", it) }
        os.blankToNull()?.let { q = q.whereArrayContains("os", it) }
        return q.get().await().toObjects(Room::class.java)
    }

    suspend fun getReservationsForRoomToday(roomId: String): List<Reservation> {
        val zone = ZoneId.systemDefault()

        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val snapshot = col.collection("reservations")
            .whereEqualTo("roomId", roomId)
            .get()
            .await()

        val all = snapshot.toObjects(Reservation::class.java)

        return all.filter { res ->
            res.startTime in startOfDay until endOfDay
        }
    }

    // Helper care transformă stringul gol în null (pentru a-l ignora ușor)
    private fun String?.blankToNull(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }
}
