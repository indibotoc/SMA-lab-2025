//package com.example.where2upt
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent { FacultiesScreen() }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FacultiesScreen() {
//    val repo = remember { FacultyRepository() }
//    val scope = rememberCoroutineScope()
//
//    var faculties by remember { mutableStateOf(listOf<Faculty>()) }
//    var loading by remember { mutableStateOf(true) }
//    var error by remember { mutableStateOf<String?>(null) }
//    var newName by remember { mutableStateOf("") }
//
//    LaunchedEffect(Unit) {
//        try {
//            faculties = repo.getAll()
//        } catch (e: Exception) {
//            error = e.message
//        } finally {
//            loading = false
//        }
//    }
//
//    Scaffold(
//        topBar = { TopAppBar(title = { Text("UPT • Facultăți") }) },
//        floatingActionButton = {
//            FloatingActionButton(onClick = {
//                scope.launch {
//                    try {
//                        if (newName.isNotBlank()) {
//                            repo.add(newName)
//                            faculties = repo.getAll()
//                            newName = ""
//                        }
//                    } catch (e: Exception) { error = e.message }
//                }
//            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
//        }
//    ) { p ->
//        Column(Modifier.padding(p).padding(12.dp)) {
//            OutlinedTextField(
//                value = newName,
//                onValueChange = { newName = it },
//                label = { Text("Nume facultate nouă") },
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(Modifier.height(16.dp))
//
//            if (loading) CircularProgressIndicator()
//            error?.let { Text("Eroare: $it") }
//
//            LazyColumn {
//                items(faculties, key = { it.id }) { f ->
//                    ElevatedCard(Modifier.padding(vertical = 6.dp)) {
//                        Text(
//                            f.name,
//                            style = MaterialTheme.typography.titleMedium,
//                            modifier = Modifier.padding(12.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

/////////////////////////////////////////////////////////////////////////////////////////////

//package com.example.where2upt
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent { UsersScreen() }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UsersScreen() {
//    val repo = remember { UserRepository() }
//    val scope = rememberCoroutineScope()
//
//    var users by remember { mutableStateOf(listOf<UserProfile>()) }
//    var selectedRole by remember { mutableStateOf("student") }
//    var error by remember { mutableStateOf<String?>(null) }
//
//    Scaffold(
//        topBar = { TopAppBar(title = { Text("UPT • Users by Role") }) }
//    ) { padding ->
//        Column(Modifier.padding(padding).padding(12.dp)) {
//            Row {
//                listOf("student", "rep", "staff", "host", "admin").forEach { role ->
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                try {
//                                    users = repo.getAllByRole(role)
//                                    selectedRole = role
//                                    error = null
//                                } catch (e: Exception) {
//                                    error = e.message
//                                }
//                            }
//                        },
//                        modifier = Modifier.padding(end = 8.dp)
//                    ) {
//                        Text(role)
//                    }
//                }
//            }
//            Spacer(Modifier.height(16.dp))
//            Text("Rol selectat: $selectedRole", style = MaterialTheme.typography.titleMedium)
//            error?.let { Text("Eroare: $it", color = MaterialTheme.colorScheme.error) }
//            LazyColumn {
//                items(users, key = { it.uid }) { user ->
//                    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
//                        Column(Modifier.padding(8.dp)) {
//                            Text(user.displayName, style = MaterialTheme.typography.titleMedium)
//                            Text("${user.email} (${user.role})", style = MaterialTheme.typography.bodySmall)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}


/////////////////////////////////////////////////////////////////////////////////////

package com.example.where2upt

import AuthScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import com.example.where2upt.Reservation
import com.example.where2upt.ui.theme.Where2UPTTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Where2UPTTheme { AppRoot() } }
    }
}

private enum class Screen { AUTH, HOME, ROOMS, ROOM_DETAILS, CREATE_RESERVATION }

@Composable
private fun AppRoot() {
    val authRepo = remember { AuthRepository() }
    val roomRepo = remember { RoomRepository() }
    var selectedHourSlot by remember { mutableStateOf<HourSlot?>(null) }
    var selectedDate by remember { mutableStateOf(org.threeten.bp.LocalDate.now()) }
    var user by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

    LaunchedEffect(Unit) { authRepo.authState().collect { user = it } }

    var screen by remember { mutableStateOf(if (user == null) Screen.AUTH else Screen.HOME) }
    LaunchedEffect(user) { screen = if (user == null) Screen.AUTH else Screen.HOME }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    // rezervările pe ziua curentă pentru sala selectată
    var reservationsToday by remember { mutableStateOf<List<Reservation>>(emptyList()) }

    when (screen) {
        Screen.AUTH -> AuthScreen(onAuthenticated = { screen = Screen.HOME })

        Screen.HOME -> {
            val profile = UPTUser(
                firstName = user?.email?.substringBefore("@") ?: "Student",
                lastName = "",
                roles = setOf(UserRole.STUDENT)
            )
            HomeScreen(
                user = profile,
                currentBuildingId = "electro",
                logo = painterResource(R.drawable.upt_logo),
                onFindRoomClick = { screen = Screen.ROOMS },
                onMyReservationsClick = { /* ... */ },
                onApproveRequestsClick = { /* ... */ }
            )
        }

        Screen.ROOMS -> {
            val roomRepo = remember { RoomRepository() }
            val buildingId = "electro"
            BackHandler { screen = Screen.HOME }
            RoomsScreen(
                currentBuildingId = buildingId,
                onSpecificSearch = roomRepo::searchSpecific,
                onPrefsSearch = roomRepo::searchByPreferences,
                onRoomClick = { room ->
                    selectedRoom = room
                    screen = Screen.ROOM_DETAILS
                }
            )
        }
        Screen.ROOM_DETAILS -> {
            val room = selectedRoom
            BackHandler { screen = Screen.ROOMS }

            LaunchedEffect(room?.id, selectedDate) {
                if (room != null) {
                    reservationsToday = roomRepo.getReservationsForRoom(room.id, selectedDate)
                } else {
                    reservationsToday = emptyList()
                }
            }

            if (room != null) {
                RoomDetailsScreen(
                    room = room,
                    reservationsToday = reservationsToday,
                    selectedDate = selectedDate, // Transmite data
                    onDateChange = { newDate -> selectedDate = newDate }, // Actualizează data
                    onBack = { screen = Screen.ROOMS },
                    onOpenCalendar = { roomId, hourSlot ->
                        if (hourSlot != null && !hourSlot.isReserved) {
                            val now = org.threeten.bp.LocalDateTime.now()
                            val slotTime = selectedDate.atTime(hourSlot.hour, 0)

                            if (slotTime.isBefore(now)) {
                                // Opțional: afișează un mesaj că nu se poate rezerva în trecut
                            } else {
                                selectedHourSlot = hourSlot
                                screen = Screen.CREATE_RESERVATION
                            }
                        }
                    }
                )
            }
        }
        Screen.CREATE_RESERVATION -> {
            val room = selectedRoom
            val slot = selectedHourSlot
            BackHandler { screen = Screen.ROOM_DETAILS }

            if (room != null && slot != null) {
                // Aici va trebui să creezi un fișier nou "CreateReservationScreen.kt"
                // sau să îl definești temporar aici.
                CreateReservationScreen(
                    room = room,
                    slot = slot,
                    date = selectedDate, // Transmitem data selectată
                    onSuccess = {
                        screen = Screen.ROOM_DETAILS // se întoarce și reîncarcă lista
                    },
                    onBack = { screen = Screen.ROOM_DETAILS }
                )
            }
        }
}
}
