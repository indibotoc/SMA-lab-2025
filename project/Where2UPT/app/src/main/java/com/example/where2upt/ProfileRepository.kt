package com.example.where2upt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.applyCanvas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ProfileRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    suspend fun processAndSaveAvatarBase64(context: Context, uri: Uri): String {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        // 1) Load bitmap
        val original: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(src)
        } else {
            val ins = context.contentResolver.openInputStream(uri)!!
            BitmapFactory.decodeStream(ins).also { ins.close() }
        }

        // 2) Center-crop to square
        val side = minOf(original.width, original.height)
        val x = (original.width - side) / 2
        val y = (original.height - side) / 2
        val square = Bitmap.createBitmap(original, x, y, side, side)

        // 3) Resize to exactly 100×100 (filter = true for quality)
        val scaled = square.scale(100, 100, filter = true)

        // 4) Encode to PNG → Base64
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        val bytes = out.toByteArray()
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        // 5) Save in Firestore as users/{uid}.photo (string)
        users.document(uid).update("photo", b64).await()

        return b64
    }

    suspend fun getUserRoles(uid: String): List<String> {
        val snap = users.document(uid).get().await()
        @Suppress("UNCHECKED_CAST")
        return (snap.get("roles") as? List<String>) ?: emptyList()
    }
    suspend fun ensureUserDoc() {
        val u = auth.currentUser ?: error("Not logged in")
        val docRef = users.document(u.uid)
        val snap = docRef.get().await()
        val displayName = u.displayName ?: u.email?.substringBefore("@")?.replace(".", " ")?.replaceFirstChar { it.uppercase() } ?: "Unknown"

        if (!snap.exists()) {
            docRef.set(
                mapOf(
                    "uid" to u.uid,
                    "email" to (u.email ?: ""),
                    "displayName" to displayName,
                    "roles" to listOf("student"),
                    "status" to "active",
                    "createdAt" to com.google.firebase.Timestamp.now()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } else {
            // dacă există dar nu are displayName
            if (snap.getString("displayName").isNullOrBlank()) {
                docRef.update("displayName", displayName).await()
            }
        }
    }

}