package com.example.where2upt

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BuildingRepository {
    private val col = FirebaseFirestore.getInstance().collection("buildings")

    suspend fun getAll(): List<Building> =
        col.get().await().toObjects(Building::class.java)
}