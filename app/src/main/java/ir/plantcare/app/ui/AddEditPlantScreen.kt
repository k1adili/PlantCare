package ir.plantcare.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.plantcare.app.data.Plant
import ir.plantcare.app.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    existingPlant: Plant?,
    onBack: () -> Unit,
    onSave: (Plant) -> Unit,
    onDelete: (Plant) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(existingPlant?.name ?: "") }
    var species by remember { mutableStateOf(existingPlant?.species ?: "") }
    var wateringDays by remember { mutableStateOf((existingPlant?.wateringIntervalDays ?: 3).toString()) }
    var fertilizingDays by remember { mutableStateOf((existingPlant?.fertilizingIntervalDays ?: 20).toString()) }
    var notes by remember { mutableStateOf(existingPlant?.notes ?: "") }
    var photoFileName by remember { mutableStateOf(existingPlant?.photoFileName) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var cameraFileNameHolder by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoFileName = ImageUtils.saveImageFromUri(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        // فایل از قبل توسط createCameraOutputUri در مسیر درست ذخیره شده،
        // فقط در صورت موفقیت نام آن را به‌عنوان عکس گیاه ثبت می‌کنیم
        if (success) {
            photoFileName = cameraFileNameHolder
        } else {
            // کاربر عکس نگرفت یا لغو کرد؛ فایل خالی احتمالی را پاک می‌کنیم
            cameraFileNameHolder?.let { ImageUtils.deleteImage(context, it) }
        }
    }

    fun openCamera() {
        try {
            val (uri, fileName) = ImageUtils.createCameraOutputUri(context)
            cameraUri = uri
            cameraFileNameHolder = fileName
            cameraLauncher.launch(uri)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(context, "هیچ برنامه دوربینی روی این دستگاه پیدا نشد", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "باز کردن دوربین ممکن نشد: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(context, "برای گرفتن عکس، اجازه دسترسی به دوربین لازم است", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingPlant == null) "افزودن گیاه" else "ویرایش گیاه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (existingPlant != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val photoFile = ImageUtils.fileFor(context, photoFileName)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🪴 عکسی انتخاب نشده", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("گالری")
                }
                OutlinedButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            openCamera()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("دوربین")
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("نام گیاه *") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = species, onValueChange = { species = it },
                label = { Text("نوع / گونه (اختیاری)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = wateringDays,
                    onValueChange = { wateringDays = it.filter { c -> c.isDigit() } },
                    label = { Text("دوره آبیاری (روز)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fertilizingDays,
                    onValueChange = { fertilizingDays = it.filter { c -> c.isDigit() } },
                    label = { Text("دوره کوددهی (روز)") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val plant = (existingPlant ?: Plant(name = "")).copy(
                        name = name.ifBlank { "گیاه بدون‌نام" },
                        species = species,
                        photoFileName = photoFileName,
                        wateringIntervalDays = wateringDays.toIntOrNull() ?: 3,
                        fertilizingIntervalDays = fertilizingDays.toIntOrNull() ?: 20,
                        notes = notes
                    )
                    onSave(plant)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) { Text("ذخیره") }
        }
    }

    if (showDeleteConfirm && existingPlant != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف گیاه") },
            text = { Text("آیا از حذف «${existingPlant.name}» و تمام تاریخچه آن مطمئن هستید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(existingPlant)
                }) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") }
            }
        )
    }
}
