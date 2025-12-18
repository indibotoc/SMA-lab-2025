package com.example.where2upt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationScreen(
    room: Room,
    slot: HourSlot,
    date: org.threeten.bp.LocalDate,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ReservationRepository(FirebaseFirestore.getInstance()) }
    var purpose by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculăm timestamp-urile pentru rezervare (presupunem data de azi)
    val startTimeMillis = remember(slot.hour, date) {
        date.atTime(slot.hour, 0)
            .atZone(org.threeten.bp.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
    val endTimeMillis = startTimeMillis + TimeUnit.HOURS.toMillis(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmă Rezervarea") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Detalii Sală
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sala ${room.blockId} ${room.roomNumber}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Interval: ${slot.hour}:00 - ${slot.hour + 1}:00")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Pentru ce ai nevoie de această sală?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Scopul rezervării (ex: Studiu individual)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (purpose.isBlank()) {
                        errorMessage = "Te rugăm să introduci scopul rezervării."
                        return@Button
                    }

                    scope.launch {
                        loading = true
                        errorMessage = null
                        try {
                            val user = FirebaseAuth.getInstance().currentUser
                            val reservation = Reservation(
                                roomId = room.id,
                                userId = user?.uid ?: "unknown",
                                createdByRole = "student", // Ar trebui preluat din profilul real
                                startTime = startTimeMillis,
                                endTime = endTimeMillis,
                                purpose = purpose,
                                status = "pending"
                            )

                            repo.createReservation(reservation)
                            onSuccess()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "A apărut o eroare la salvare."
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmă Rezervarea", fontSize = 18.sp)
                }
            }
        }
    }
}