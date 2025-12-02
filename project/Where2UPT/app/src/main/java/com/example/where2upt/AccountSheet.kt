package com.example.where2upt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(
    onSeeReservations: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current  // ✅ safe to use here (inside composable)
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val repo = remember { ProfileRepository() }
    val scope = rememberCoroutineScope()

    var photoB64 by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf<String>("") }
    var roles by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // --- Load profile from Firestore (handles random doc IDs & creates if missing)
    LaunchedEffect(user?.uid) {
        runCatching {
            repo.ensureUserDoc() // create if missing & fill displayName if empty

            val uid = user?.uid ?: return@runCatching
            val users = FirebaseFirestore.getInstance().collection("users")

            // prefer /users/{uid}; if absent, fall back to query by uid field
            val byId = users.document(uid).get().await()
            val doc = if (byId.exists()) byId else {
                val q = users.whereEqualTo("uid", uid).limit(1).get().await()
                if (!q.isEmpty) q.documents.first() else byId
            }

            photoB64 = doc.getString("photo")
            displayName = doc.getString("displayName")
                ?: user?.displayName
                        ?: user?.email?.substringBefore("@")?.replace(".", " ")?.replaceFirstChar { it.uppercase() }
                        ?: "User"
            @Suppress("UNCHECKED_CAST")
            roles = (doc.get("roles") as? List<String>).orEmpty()
        }.onFailure { error = it.message }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploading = true; error = null
                runCatching {
                    repo.processAndSaveAvatarBase64(ctx, uri)  // ✅ pass ctx safely
                }.onSuccess { photoB64 = it }
                    .onFailure { error = it.message }
                uploading = false
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

            // Avatar 100×100 (Base64 in Firestore)
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatarBase64(photoBase64 = photoB64, onClick = { launcher.launch("image/*") })
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { launcher.launch("image/*") }, enabled = !uploading) {
                Text(if (uploading) "Uploading..." else "Change photo")
            }

            Spacer(Modifier.height(8.dp))
            Text(displayName, style = MaterialTheme.typography.titleMedium)
            Text(auth.currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(12.dp))
            Text(
                "Role(s): ${if (roles.isEmpty()) "student" else roles.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onDismiss(); onSeeReservations() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) { Text("See my reservations & requests") }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onDismiss(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) { Text("Log out") }

            Spacer(Modifier.height(10.dp))
        }
    }
}
