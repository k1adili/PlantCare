package ir.plantcare.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onExport: (android.net.Uri, (Boolean, String?) -> Unit) -> Unit,
    onImport: (android.net.Uri, (Boolean, String?) -> Unit) -> Unit
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            onExport(uri) { success, error ->
                Toast.makeText(
                    context,
                    if (success) "بکاپ با موفقیت ذخیره شد" else "خطا در بکاپ‌گیری: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImport(uri) { success, error ->
                Toast.makeText(
                    context,
                    if (success) "اطلاعات با موفقیت بازیابی شد" else "خطا در بازیابی: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("پشتیبان‌گیری", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "از تمام گیاهان، تاریخچه و عکس‌ها یک فایل zip می‌سازد که می‌توانید در گوگل‌درایو یا حافظه گوشی ذخیره کنید.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { exportLauncher.launch("plantcare_backup.zip") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("گرفتن بکاپ") }

            Spacer(Modifier.height(24.dp))
            Text("بازیابی از بکاپ", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "توجه: بازیابی، تمام اطلاعات فعلی را با اطلاعات فایل بکاپ جایگزین می‌کند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { importLauncher.launch("application/zip") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("انتخاب فایل بکاپ و بازیابی") }
        }
    }
}
