package com.example.where2upt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FacultiesScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultiesScreen() {
    val repo = remember { FacultyRepository() }
    val scope = rememberCoroutineScope()

    var faculties by remember { mutableStateOf(listOf<Faculty>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            faculties = repo.getAll()
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("UPT • Facultăți") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    try {
                        if (newName.isNotBlank()) {
                            repo.add(newName)
                            faculties = repo.getAll()
                            newName = ""
                        }
                    } catch (e: Exception) { error = e.message }
                }
            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { p ->
        Column(Modifier.padding(p).padding(12.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nume facultate nouă") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            if (loading) CircularProgressIndicator()
            error?.let { Text("Eroare: $it") }

            LazyColumn {
                items(faculties, key = { it.id }) { f ->
                    ElevatedCard(Modifier.padding(vertical = 6.dp)) {
                        Text(
                            f.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}