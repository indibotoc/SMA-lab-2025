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
import androidx.compose.runtime.remember
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
import java.util.Locale

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
    background: Painter = painterResource(R.drawable.bg_upt), // pune o poză UPT
    logo: Painter = painterResource(R.drawable.upt_logo),       // pune logo-ul UPT
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
    val greeting = remember(locale) { greetingFor(locale) } // „Welcome” / „Bună” / etc.

    // 2) Are drept de aprobare?
    val canApprove = remember(user.roles) {
        user.roles.intersect(setOf(UserRole.ROOM_HOST, UserRole.FACULTY_ADMIN, UserRole.SUPER_ADMIN)).isNotEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fundal foto
        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: logo UPT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = logo,
                    contentDescription = "UPT",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                    text = user.fullName + "!",
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
                        badge = "•", // simplu indicator; poți conecta un număr real
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

private fun greetingFor(locale: Locale): String = when (locale.language.lowercase(Locale.ROOT)) {
    "ro" -> "Bună"
    "fr" -> "Bonjour"
    "de" -> "Hallo"
    "it" -> "Ciao"
    "es" -> "Hola"
    else -> "Welcome"
}
