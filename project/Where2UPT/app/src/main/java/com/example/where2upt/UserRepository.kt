package com.example.where2upt


import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val users = db.collection("users")

    suspend fun createUser(profile: UserProfile) {
        users.document(profile.uid).set(profile).await()
    }

    suspend fun getUser(uid: String): UserProfile? {
        val snap = users.document(uid).get().await()
        return snap.toObject(UserProfile::class.java)
    }

    suspend fun getAllByRole(role: String): List<UserProfile> {
        val snap = users.whereEqualTo("role", role).get().await()
        return snap.documents.mapNotNull { it.toObject(UserProfile::class.java) }
    }

    suspend fun updateRole(uid: String, newRole: String) {
        users.document(uid).update("role", newRole).await()
    }
}
