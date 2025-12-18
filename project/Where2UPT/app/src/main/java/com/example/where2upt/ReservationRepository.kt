package com.example.where2upt

import com.example.where2upt.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReservationRepository(
    private val db: FirebaseFirestore
) {
    suspend fun createReservation(res: Reservation): String {
        require(res.isValidHourMultiple()) { "Reservation must be in whole hours and aligned to the hour." }

        val col = db.collection("rooms")
            .document(res.roomId)
            .collection("reservations")

        val newDoc = col.document() // id nou
        val now = System.currentTimeMillis()

        val overlapQuery = col
            // 1. Primul filtru de gamă
            .whereLessThan("startTime", res.endTime)
            // 2. Al doilea filtru de gamă
            .whereGreaterThan("endTime", res.startTime)
            // 3. Ordonarea TREBUIE să urmeze ordinea filtrelor de mai sus
            .orderBy("startTime")
            .orderBy("endTime")

        val snap = overlapQuery.get().await()

// The query already filters for overlaps, so we just check if any documents were returned
        val overlaps = snap.documents.isNotEmpty()

        if (overlaps) throw IllegalStateException("Time slot is already reserved.")


// 2. PERFORM ATOMIC WRITE
        db.runTransaction { tx ->
            // NO read is necessary here since the check was done above.
            // The transaction now only performs the set operation atomically.

            val data = hashMapOf(
                "id" to newDoc.id,
                "roomId" to res.roomId,
                "userId" to res.userId,
                "createdByRole" to res.createdByRole,
                "startTime" to res.startTime,
                "endTime" to res.endTime,
                "purpose" to res.purpose,
                "status" to res.status,
                "createdAt" to now,
                "updatedAt" to now
            )

            tx.set(newDoc, data)
            newDoc.id // Return the new ID from the transaction
        }.await()

        return newDoc.id
    }
}