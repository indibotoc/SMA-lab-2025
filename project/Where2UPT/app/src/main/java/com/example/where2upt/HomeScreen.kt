package com.example.where2upt

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.RuleFolder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.where2upt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.tasks.await

// ---- Roles (ajustează denumirile la modelul tău real)
enum class UserRole { STUDENT, TEACHER, ROOM_HOST, FACULTY_ADMIN, SUPER_ADMIN }

data class UPTUser(
    val firstName: String,
    val lastName: String,
    val roles: Set<UserRole>
) {
    val fullName: String get() = "$firstName $lastName"
}

@Composable
fun HomeScreen(
    user: UPTUser,
    currentBuildingId: String,
    logo: Painter = painterResource(R.drawable.upt_logo),
    onFindRoomClick: () -> Unit,
    onMyReservationsClick: () -> Unit,
    onApproveRequestsClick: () -> Unit,
) {

    // 1) Salutare în limba telefonului
    val ctx = LocalContext.current
    val locale: Locale = remember {
        if (Build.VERSION.SDK_INT >= 24)
            ctx.resources.configuration.locales[0]
        else
            @Suppress("DEPRECATION") ctx.resources.configuration.locale
    }
    val greeting = remember(locale) { greetingFor(locale) }

    // 2) Are drept de aprobare?
    val canApprove = remember(user.roles) {
        user.roles.intersect(setOf(UserRole.ROOM_HOST, UserRole.FACULTY_ADMIN, UserRole.SUPER_ADMIN)).isNotEmpty()
    }

    // 3) Avatar (Base64) & sheet state
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val uid = firebaseUser?.uid
    var photoB64 by remember { mutableStateOf<String?>(null) }

    var shortName by remember {
        mutableStateOf(
            firstNameOf(
                // fallback local dacă nu avem încă din Firestore/Auth
                user.firstName.ifBlank { user.fullName }
            )
        )
    }

    // Load current base64 avatar from Firestore
    LaunchedEffect(uid) {
        uid?.let {
            val snap = FirebaseFirestore.getInstance()
                .collection("users").document(it).get().await()
            photoB64 = snap.getString("photo")

            val displayNameFromDb = snap.getString("displayName")
                ?: firebaseUser?.displayName
                ?: firebaseUser?.email
                    ?.substringBefore("@")
                    ?.replace(".", " ")
                    ?.replaceFirstChar { c -> c.uppercase() }

            displayNameFromDb?.let { dn ->
                shortName = firstNameOf(dn)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fundal foto
        com.example.where2upt.geo.CampusBackground(
            buildingId = currentBuildingId,
            modifier = Modifier.fillMaxSize()
        )

        // Scrim + coloană conținut
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xAA000000), // stânga mai închis
                            Color(0x33000000)  // dreapta mai transparent
                        )
                    )
                )
        )

        var showAccount by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: logo UPT (stânga) + avatar (dreapta, Base64)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = logo,
                    contentDescription = "UPT",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                ProfileAvatarBase64(
                    photoBase64 = photoB64,
                    onClick = { showAccount = true },
                    modifier = Modifier
                )
            }

            // Salut + acțiuni
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "$greeting,",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$shortName!",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 48.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                TransparentActionButton(
                    title = when (locale.language) {
                        "ro" -> "Găsește o sală potrivită pentru tine"
                        else -> "Find the perfect room for you"
                    },
                    iconLeft = Icons.Default.MeetingRoom,
                    onClick = onFindRoomClick
                )

                TransparentActionButton(
                    title = when (locale.language) {
                        "ro" -> "Vezi rezervările și requesturile tale"
                        else -> "View your reservations & requests"
                    },
                    iconLeft = Icons.Default.EventNote,
                    onClick = onMyReservationsClick
                )

                if (canApprove) {
                    TransparentActionButton(
                        title = when (locale.language) {
                            "ro" -> "Requesturi de aprobat"
                            else -> "Requests to approve"
                        },
                        iconLeft = Icons.Default.RuleFolder,
                        badge = "•",
                        onClick = onApproveRequestsClick
                    )
                }
            }

            // Picior de pagină discret
            Text(
                text = when (locale.language) {
                    "ro" -> "Universitatea Politehnica Timișoara"
                    else -> "Politehnica University of Timișoara"
                },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp).alpha(0.9f)
            )
        }

        // Account bottom sheet (upload→resize→base64 handled inside AccountSheet/ProfileRepository)
        if (showAccount) {
            AccountSheet(
                onSeeReservations = onMyReservationsClick,
                onLogout = { AuthRepository().logout() },
                onDismiss = {
                    showAccount = false
                    // refresh avatar after sheet closes (in case it changed)
                    // Re-read photo field:
                    uid?.let {
                        // fire-and-forget refresh; you can also use addSnapshotListener if you want live updates
                        kotlinx.coroutines.GlobalScope.launch {
                            val snap = FirebaseFirestore.getInstance()
                                .collection("users").document(it).get().await()
                            photoB64 = snap.getString("photo")
                        }
                    }
                }
            )
        }
    }
}


@Composable
private fun TransparentActionButton(
    title: String,
    iconLeft: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(18.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.2.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(iconLeft, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 16.sp)
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(badge, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

private fun firstNameOf(full: String): String {
    val parts = full.trim().split(Regex("\\s+"))
    val candidate = parts.lastOrNull().orEmpty()
    return candidate.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun greetingFor(locale: Locale): String = when (locale.language.lowercase(Locale.ROOT)) {
    "ro" -> "Bună"
    "fr" -> "Bonjour"
    "de" -> "Hallo"
    "it" -> "Ciao"
    "es" -> "Hola"
    else -> "Welcome"
}
