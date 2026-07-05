package ir.plantcare.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.plantcare.app.data.CareLog
import ir.plantcare.app.data.CareType
import ir.plantcare.app.data.Plant
import ir.plantcare.app.util.ImageUtils
import ir.plantcare.app.util.JalaliCalendar

/** نگه‌داری وضعیت دیالوگ ثبت/ویرایش رویداد. اگر editingLog پر باشد یعنی در حالت ویرایش هستیم. */
private data class LogDialogState(val type: CareType, val editingLog: CareLog? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: Plant,
    logs: List<CareLog>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLogCare: (CareType, Long, String) -> Unit,
    onUpdateLog: (CareLog, CareType, Long, String) -> Unit,
    onDeleteLog: (CareLog) -> Unit
) {
    val context = LocalContext.current
    val photoFile = ImageUtils.fileFor(context, plant.photoFileName)
    var dialogState by remember { mutableStateOf<LogDialogState?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "ویرایش گیاه")
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
                    Button(
                        onClick = { dialogState = LogDialogState(CareType.WATERING) },
                        modifier = Modifier.weight(1f)
                    ) { Text("ثبت آبیاری") }
                    Button(
                        onClick = { dialogState = LogDialogState(CareType.FERTILIZING) },
                        modifier = Modifier.weight(1f)
                    ) { Text("ثبت کوددهی") }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { dialogState = LogDialogState(CareType.PRUNING) },
                        modifier = Modifier.weight(1f)
                    ) { Text("هرس") }
                    OutlinedButton(
                        onClick = { dialogState = LogDialogState(CareType.REPOTTING) },
                        modifier = Modifier.weight(1f)
                    ) { Text("تعویض گلدان") }
                    OutlinedButton(
                        onClick = { dialogState = LogDialogState(CareType.OTHER) },
                        modifier = Modifier.weight(1f)
                    ) { Text("سایر") }
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
                    LogItem(
                        log = log,
                        onEdit = { dialogState = LogDialogState(log.type, editingLog = log) },
                        onDelete = { onDeleteLog(log) }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    dialogState?.let { state ->
        val editingLog = state.editingLog
        var note by remember(editingLog) { mutableStateOf(editingLog?.note ?: "") }
        var pickedDate by remember(editingLog) {
            mutableStateOf(editingLog?.dateMillis ?: System.currentTimeMillis())
        }
        var showDatePicker by remember { mutableStateOf(false) }
        val isEditing = editingLog != null

        AlertDialog(
            onDismissRequest = { dialogState = null },
            title = { Text(if (isEditing) "ویرایش ${state.type.label}" else "ثبت ${state.type.label}") },
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
                    if (isEditing) {
                        onUpdateLog(editingLog!!, state.type, pickedDate, note)
                    } else {
                        onLogCare(state.type, pickedDate, note)
                    }
                    dialogState = null
                }) { Text(if (isEditing) "ذخیره" else "ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { dialogState = null }) { Text("انصراف") }
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
private fun LogItem(log: CareLog, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(log.type.label, fontWeight = FontWeight.Bold)
                Text(JalaliCalendar.format(log.dateMillis), style = MaterialTheme.typography.bodySmall)
                if (log.note.isNotBlank()) {
                    Text(
                        log.note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف")
            }
        }
    }
}
