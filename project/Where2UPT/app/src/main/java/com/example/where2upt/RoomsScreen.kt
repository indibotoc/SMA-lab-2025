package com.example.where2upt

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun RoomsScreen(
    currentBuildingId: String,
    onSpecificSearch: suspend (buildingId: String?, blockId: String?, floor: Int?, roomNumber: String?) -> List<Room>,
    onPrefsSearch: suspend (minCapacity: Int?, hasComputers: Boolean?, os: String?) -> List<Room>,
    onRoomClick: (Room) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    val buildingRepo = remember { BuildingRepository() }
    var buildings by remember { mutableStateOf<List<Building>>(emptyList()) }
    var buildingsLoading by remember { mutableStateOf(true) }
    var buildingsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { buildingRepo.getAll() }
            .onSuccess { buildings = it; buildingsError = null }
            .onFailure { buildingsError = it.message }
        buildingsLoading = false
    }

    val tabs = listOf("Find a specific room", "Find a room by preferences")

    Box(Modifier.fillMaxSize()) {
        com.example.where2upt.geo.CampusBackground(
            buildingId = currentBuildingId,
            modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x99000000), Color(0x33000000)))
            )
        )

        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
            Text("Find a room", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]).height(3.dp),
                        color = Color.White
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { i, t ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f),
                        text = { Text(t, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        icon = {
                            if (i == 0) Icon(Icons.Filled.MeetingRoom, null)
                            else Icon(Icons.Filled.Tune, null)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            when (selectedTab) {
                0 -> SpecificSearchSection(
                    buildings = buildings,
                    buildingsLoading = buildingsLoading,
                    buildingsError = buildingsError,
                    onSpecificSearch = onSpecificSearch,
                    onRoomClick = onRoomClick
                )
                1 -> PreferenceSearchSection(onPrefsSearch, onRoomClick)
            }
        }
    }
}

@Composable
private fun SpecificSearchSection(
    buildings: List<Building>,
    buildingsLoading: Boolean,
    buildingsError: String?,
    // UPDATED: now includes blockId
    onSpecificSearch: suspend (buildingId: String?, blockId: String?, floor: Int?, roomNumber: String?) -> List<Room>,
    onRoomClick: (Room) -> Unit
) {
    var selectedBuildingId by remember { mutableStateOf<String?>(null) }
    var selectedBlockId by remember { mutableStateOf<String?>(null) }  // NEW
    var floorText by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Room>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // blocks of current building
    val currentBlocks: List<Block> by remember(selectedBuildingId, buildings) {
        mutableStateOf(buildings.firstOrNull { it.id == selectedBuildingId }?.blocks ?: emptyList())
    }

    // reset block if building changes
    LaunchedEffect(selectedBuildingId) { selectedBlockId = null }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (buildingsLoading) {
            Text("Loading buildings…", color = Color.White)
        } else {
            BuildingDropdown(
                items = buildings,
                selectedId = selectedBuildingId,
                onSelected = { selectedBuildingId = it },
                label = "Building"
            )
            Spacer(Modifier.height(6.dp))
            BlockDropdown(
                items = currentBlocks,
                selectedId = selectedBlockId,
                onSelected = { selectedBlockId = it },
                enabled = selectedBuildingId != null && currentBlocks.isNotEmpty(),
                label = "Block"
            )
            buildingsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        TransparentField(
            floorText,
            { floorText = it.filter { ch -> ch.isDigit() || ch == '-' } },
            "Floor (e.g., 1, 2, 0)"
        )
        TransparentField(roomNumber, { roomNumber = it }, "Room number (e.g., 02, 03B)")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton("Search") {
                scope.launch {
                    loading = true
                    try {
                        val floor = floorText.toIntOrNull()
                        val rn = roomNumber.trim().uppercase().ifEmpty { null }
                        results = onSpecificSearch(selectedBuildingId, selectedBlockId, floor, rn)
                        error = null
                    } catch (e: Exception) {
                        error = e.message
                        results = emptyList()
                    } finally {
                        loading = false
                    }
                }
            }
            if (selectedBuildingId != null || selectedBlockId != null ||
                floorText.isNotBlank() || roomNumber.isNotBlank() || results.isNotEmpty()
            ) {
                GlassButtonOutlined("Clear") {
                    selectedBuildingId = null
                    selectedBlockId = null
                    floorText = ""
                    roomNumber = ""
                    results = emptyList()
                    error = null
                }
            }
        }

        if (loading) CircularProgressIndicator(color = Color.White)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        ResultsList(results, onRoomClick)
    }
}

@Composable
private fun PreferenceSearchSection(
    onPrefsSearch: suspend (minCapacity: Int?, hasComputers: Boolean?, os: String?) -> List<Room>,
    onRoomClick: (Room) -> Unit
) {
    var capacity by remember { mutableStateOf(30f) }
    var requirePC by remember { mutableStateOf<Boolean?>(null) }
    var os by remember { mutableStateOf<String?>(null) } // list in model; filter with contains(os)
    var loading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Room>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Minimum seats: ${capacity.toInt()}", color = Color.White, fontSize = 14.sp)
        Slider(value = capacity, onValueChange = { capacity = it }, valueRange = 0f..300f)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChipTernary("PCs", requirePC, onChange = { requirePC = it })
            OSFilter(os) { os = it }  // Any / Windows / Linux / macOS
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton("Search") {
                scope.launch {
                    loading = true
                    results = runCatching {
                        onPrefsSearch(capacity.toInt(), requirePC, os)
                    }.getOrDefault(emptyList())
                    loading = false
                }
            }
            if (results.isNotEmpty() || capacity.toInt() != 30 || requirePC != null || os != null) {
                GlassButtonOutlined("Clear") {
                    capacity = 30f; requirePC = null; os = null; results = emptyList()
                }
            }
        }

        if (loading) CircularProgressIndicator(color = Color.White)
        ResultsList(results, onRoomClick)
    }
}

/* ===== reusable UI bits ===== */

@Composable
private fun TransparentField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White.copy(alpha = 0.7f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
            cursorColor = Color.White
        )
    )
}

@Composable private fun GlassButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.16f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) { Text(text) }
}

@Composable private fun GlassButtonOutlined(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) { Text(text) }
}

@Composable
private fun ResultsList(results: List<Room>, onRoomClick: (Room) -> Unit) {
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        items(results, key = { it.id }) { room ->
            RoomCard(room) { onRoomClick(room) }
        }
    }
}

@Composable
private fun RoomCard(room: Room, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.10f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.MeetingRoom, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${room.buildingId} • floor ${room.floor}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
                Text(
                    text = "${room.blockId.substringAfter('-').uppercase()}${room.floor}${room.roomNumber}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val osLabel = if (room.os.isNotEmpty()) room.os.joinToString(limit = 2) else "no OS"
                val appsLabel = if (room.apps.isNotEmpty()) " • ${room.apps.size} apps" else ""
                val pcLabel = if (room.hasComputers) " • PCs ${room.pcCount}" else ""
                Text(
                    text = "${room.capacity} seats$pcLabel • $osLabel$appsLabel",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (room.status != "active") {
                    Text(
                        text = "Status: ${room.status}",
                        color = Color(0xFFFFE082),
                        fontSize = 12.sp
                    )
                }
            }
            if (room.hasComputers) {
                Icon(Icons.Filled.Computer, contentDescription = null, tint = Color.White)
            }
        }
    }
}

/* ===== filters ===== */

@Composable
private fun OSFilter(current: String?, onChange: (String?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilterChip("Any", current == null) { onChange(null) }
        FilterChip("Windows", current == "Windows") { onChange("Windows") }
        FilterChip("Linux", current == "Linux") { onChange("Linux") }
        FilterChip("macOS", current == "macOS") { onChange("macOS") }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { onClick() },
        color = if (selected) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.10f)
    ) {
        Text(label, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 13.sp)
    }
}

/** null = Any, true = must have PCs, false = no PCs */
@Composable
private fun FilterChipTernary(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label:", color = Color.White)
        FilterChip("Any", value == null) { onChange(null) }
        FilterChip("Yes", value == true) { onChange(true) }
        FilterChip("No", value == false) { onChange(false) }
    }
}

/* ===== Block dropdown (local, simple) ===== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockDropdown(
    items: List<Block>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
    enabled: Boolean,
    label: String = "Block"
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = items.firstOrNull { it.id == selectedId }?.name ?: ""
    val displayText = if (selectedName.isNotBlank()) "$selectedName ($selectedId)" else ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.7f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                cursorColor = Color.White
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Any block") }, onClick = { onSelected(null); expanded = false })
            items.forEach { bl ->
                DropdownMenuItem(
                    text = { Text("${bl.name} (${bl.id})") },
                    onClick = { onSelected(bl.id); expanded = false }
                )
            }
        }
    }
}
