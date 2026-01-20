package com.example.where2upt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.where2upt.Reservation
import com.google.firebase.Timestamp
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.Instant


fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.ofEpochMilli(this)
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}
private fun Timestamp.toLocalDateTimeThreeten(): LocalDateTime {
    val zone = ZoneId.systemDefault()
    val instant = Instant.ofEpochMilli(this.toDate().time)
    return LocalDateTime.ofInstant(instant, zone)
}

fun buildHourSlotsForDay(
    date: LocalDate,
    reservations: List<Reservation>,
    startHour: Int,
    endHour: Int
): List<HourSlot> {
    val zone = ZoneId.systemDefault()

    return (startHour until endHour).map { hour ->
        val slotStart = date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
        val slotEnd = slotStart + 3600000 // +1 oră fix

        val slotReservation = reservations.firstOrNull { res ->
            // Logică de suprapunere strictă: StartA < EndB AND EndA > StartB
            res.startTime < slotEnd && res.endTime > slotStart
        }

        HourSlot(
            hour = hour,
            isReserved = slotReservation != null,
            reservationTitle = slotReservation?.purpose,
            reservationId = slotReservation?.id
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomDetailsScreen(
    room: Room,
    reservationsToday: List<Reservation>, // Acestea vor fi rezervările pentru data selectată
    selectedDate: LocalDate, // Parametru nou pentru a ști ce zi afișăm
    onDateChange: (LocalDate) -> Unit, // Callback pentru a schimba data din MainActivity
    onBack: () -> Unit,
    onOpenCalendar: (roomId: String, hourSlot: HourSlot?) -> Unit
) {
    val hourSlots = remember(reservationsToday, selectedDate) {
        buildHourSlotsForDay(
            date = selectedDate,
            reservations = reservationsToday,
            startHour = 8,
            endHour = 22
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Sălile de ales") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Înapoi"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // TOT conținutul trebuie să fie în acest Column pentru a fi interactiv și scrollabil
        Column(
            modifier = Modifier
                .padding(padding) // Respectă înălțimea TopAppBar-ului
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. MUTĂ selectorul de dată AICI (în interiorul coloanei)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDateChange(selectedDate.minusDays(1)) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Ziua precedentă")
                }
                Text(
                    text = selectedDate.toString(),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { onDateChange(selectedDate.plusDays(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Ziua următoare")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            RoomHeader(room = room)
            Spacer(modifier = Modifier.height(16.dp))
            RoomPhotosCarousel(photoUrls = room.photoUrls)
            Spacer(modifier = Modifier.height(16.dp))
            RoomAttributesSection(room = room)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Disponibilitate",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            RoomDayTimeline(
                slots = hourSlots,
                onHourClick = { slot ->
                    onOpenCalendar(room.id, slot)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Atinge un interval pentru a rezerva.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onOpenCalendar(room.id, null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Vezi calendarul complet")
            }
        }
    }
}

@Composable
fun RoomHeader(room: Room) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Sala ${room.blockId + room.floor + room.roomNumber}",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Clădire: ${room.buildingId} • Etaj ${room.floor}",
            style = MaterialTheme.typography.bodyMedium
        )
        if (room.status != "active") {
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = { },
                label = { Text(room.status.uppercase()) }
            )
        }
    }
}

@Composable
fun RoomPhotosCarousel(
    photoUrls: List<String>
) {
    if (photoUrls.isEmpty()) return

    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .height(160.dp)
            .fillMaxWidth()
    ) {
        items(photoUrls.size) { index ->
            val url = photoUrls[index]
            androidx.compose.material3.Card(
                modifier = Modifier
                    .width(220.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Poză sală",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomAttributesSection(room: Room) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Detalii sală",
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = {},
                label = { Text("Capacitate: ${room.capacity}") }
            )

            if (room.hasComputers) {
                AssistChip(
                    onClick = {},
                    label = { Text("PC-uri: ${room.pcCount}") }
                )
            }

            if (room.studentReservable) {
                AssistChip(
                    onClick = {},
                    label = { Text("Rezervabilă de studenți") }
                )
            }

            room.os.forEach { os ->
                AssistChip(
                    onClick = {},
                    label = { Text(os) }
                )
            }

            room.apps.forEach { app ->
                AssistChip(
                    onClick = {},
                    label = { Text(app) }
                )
            }
        }
    }
}

@Composable
fun RoomDayTimeline(
    slots: List<HourSlot>,
    onHourClick: (HourSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // etichete orientative (optional)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "08:00", style = MaterialTheme.typography.labelSmall)
            Text(text = "12:00", style = MaterialTheme.typography.labelSmall)
            Text(text = "16:00", style = MaterialTheme.typography.labelSmall)
            Text(text = "20:00", style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            slots.forEach { slot ->
                val color = if (slot.isReserved) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 1.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color)
                        .clickable { onHourClick(slot) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${slot.hour}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}
