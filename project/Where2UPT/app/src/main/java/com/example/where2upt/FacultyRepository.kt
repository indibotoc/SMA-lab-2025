package com.example.where2upt

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FacultyRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val col = db.collection("faculties")

    suspend fun getAll(): List<Faculty> {
        val snap = col.orderBy("name").get().await()
        return snap.documents.mapNotNull { it.toObject(Faculty::class.java)?.copy(id = it.id) }
    }

    suspend fun add(name: String) {
        val doc = col.document()
        val f = Faculty(id = doc.id, name = name.trim())
        doc.set(f).await()
    }
}
