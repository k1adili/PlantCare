package ir.plantcare.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.plantcare.app.data.CareLog
import ir.plantcare.app.data.CareType
import ir.plantcare.app.data.Plant
import ir.plantcare.app.util.ImageUtils
import ir.plantcare.app.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: Plant,
    logs: List<CareLog>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLogCare: (CareType, Long, String) -> Unit,
    onDeleteLog: (CareLog) -> Unit
) {
    val context = LocalContext.current
    val photoFile = ImageUtils.fileFor(context, plant.photoFileName)
    var showLogDialog by remember { mutableStateOf<CareType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoFile != null) {
                        AsyncImage(
                            model = photoFile, contentDescription = plant.name,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("🪴", style = MaterialTheme.typography.displayLarge)
                    }
                }
            }
            item {
                Column(Modifier.padding(16.dp)) {
                    if (plant.species.isNotBlank()) {
                        Text(plant.species, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    InfoRow("دوره آبیاری", "هر ${plant.wateringIntervalDays} روز")
                    InfoRow(
                        "آبیاری بعدی",
                        plant.nextWateringMillis()?.let { JalaliCalendar.format(it) } ?: "ثبت نشده"
                    )
                    InfoRow("دوره کوددهی", "هر ${plant.fertilizingIntervalDays} روز")
                    InfoRow(
                        "کوددهی بعدی",
                        plant.nextFertilizingMillis()?.let { JalaliCalendar.format(it) } ?: "ثبت نشده"
                    )
                    if (plant.notes.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("یادداشت: ${plant.notes}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { showLogDialog = CareType.WATERING }, modifier = Modifier.weight(1f)) {
                        Text("ثبت آبیاری")
                    }
                    Button(onClick = { showLogDialog = CareType.FERTILIZING }, modifier = Modifier.weight(1f)) {
                        Text("ثبت کوددهی")
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { showLogDialog = CareType.PRUNING }, modifier = Modifier.weight(1f)) {
                        Text("هرس")
                    }
                    OutlinedButton(onClick = { showLogDialog = CareType.REPOTTING }, modifier = Modifier.weight(1f)) {
                        Text("تعویض گلدان")
                    }
                    OutlinedButton(onClick = { showLogDialog = CareType.OTHER }, modifier = Modifier.weight(1f)) {
                        Text("سایر")
                    }
                }
            }
            item {
                Text(
                    "تاریخچه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            if (logs.isEmpty()) {
                item {
                    Text(
                        "هنوز کاری ثبت نشده است.",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(logs) { log ->
                    LogItem(log = log, onDelete = { onDeleteLog(log) })
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    showLogDialog?.let { type ->
        var note by remember { mutableStateOf("") }
        var pickedDate by remember { mutableStateOf(System.currentTimeMillis()) }
        var showDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showLogDialog = null },
            title = { Text("ثبت ${type.label}") },
            text = {
                Column {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text("تاریخ: ${JalaliCalendar.format(pickedDate)}")
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = note, onValueChange = { note = it },
                        label = { Text("یادداشت (اختیاری)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onLogCare(type, pickedDate, note)
                    showLogDialog = null
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = null }) { Text("انصراف") }
            }
        )

        if (showDatePicker) {
            JalaliDatePickerDialog(
                initialMillis = pickedDate,
                onDismiss = { showDatePicker = false },
                onConfirm = { pickedDate = it; showDatePicker = false }
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LogItem(log: CareLog, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(log.type.label, fontWeight = FontWeight.Bold)
                Text(JalaliCalendar.format(log.dateMillis), style = MaterialTheme.typography.bodySmall)
                if (log.note.isNotBlank()) {
                    Text(log.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = onDelete) { Text("حذف") }
        }
    }
}
