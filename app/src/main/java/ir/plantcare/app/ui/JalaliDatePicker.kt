package ir.plantcare.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.plantcare.app.util.JalaliCalendar

@Composable
fun JalaliDatePickerDialog(
    initialMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initial = JalaliCalendar.toJalali(initialMillis)
    var year by remember { mutableIntStateOf(initial[0]) }
    var month by remember { mutableIntStateOf(initial[1]) }
    var day by remember { mutableIntStateOf(initial[2]) }

    val years = (year - 5..year + 1).toList()
    val months = (1..12).toList()
    val maxDay = JalaliCalendar.daysInJalaliMonth(year, month)
    val days = (1..maxDay).toList()
    if (day > maxDay) day = maxDay

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب تاریخ") },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PickerColumn("روز", days, day) { day = it }
                PickerColumn("ماه", months, month, labelFor = { JalaliCalendar.monthNames[it - 1] }) { month = it }
                PickerColumn("سال", years, year) { year = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(JalaliCalendar.toEpochMillis(year, month, day))
            }) { Text("تأیید") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun PickerColumn(
    label: String,
    values: List<Int>,
    selected: Int,
    labelFor: (Int) -> String = { it.toString() },
    onSelect: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        LazyColumn(
            Modifier
                .height(160.dp)
                .width(90.dp)
        ) {
            items(values) { v ->
                val isSelected = v == selected
                Text(
                    text = labelFor(v),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(v) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
