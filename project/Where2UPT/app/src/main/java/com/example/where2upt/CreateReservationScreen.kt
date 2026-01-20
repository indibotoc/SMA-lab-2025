package com.example.where2upt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Remove
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
    date: LocalDate,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ReservationRepository(FirebaseFirestore.getInstance()) }

    // Stări pentru formular
    var purpose by remember { mutableStateOf("") }
    var durationHours by remember { mutableIntStateOf(1) } // X ore
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calculăm timpii în milisecunde (Long)
    // Start-ul este fix pe baza slotului apăsat
    val startTimeMillis = remember(slot.hour, date) {
        date.atTime(slot.hour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    // End-ul depinde de numărul de ore selectat (durationHours)
    val endTimeMillis = startTimeMillis + (durationHours * 3600000L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouă Rezervare") },
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
            // Card rezumat selecție
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Sala: ${room.blockId}${room.roomNumber}", fontWeight = FontWeight.Bold)
                    Text("Data: $date")
                    Text(
                        "Interval: ${slot.hour}:00 - ${slot.hour + durationHours}:00",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // SELECTOR NUMĂR ORE (Input de tip numeric cu butoane)
            Text("Pentru câte ore rezervi?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { if (durationHours > 1) durationHours-- },
                    enabled = !loading
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus")
                }

                Surface(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$durationHours",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { if (durationHours < 6) durationHours++ }, // Limită de 6 ore
                    enabled = !loading
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Câmp scop rezervare
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Scopul rezervării (ex: Proiect SMA)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Introdu motivul...") },
                enabled = !loading
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            // Buton confirmare
            Button(
                onClick = {
                    if (purpose.isBlank()) {
                        errorMessage = "Te rugăm să introduci scopul."
                        return@Button
                    }

                    scope.launch {
                        loading = true
                        errorMessage = null
                        try {
                            val user = FirebaseAuth.getInstance().currentUser
                            val reservation = Reservation(
                                roomId = room.id,
                                userId = user?.uid ?: "anonim",
                                createdByRole = "student",
                                startTime = startTimeMillis,
                                endTime = endTimeMillis,
                                purpose = purpose,
                                status = "pending"
                            )

                            repo.createReservation(reservation)
                            onSuccess()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Eroare la salvare."
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmă Rezervarea", fontSize = 16.sp)
                }
            }
        }
    }
}