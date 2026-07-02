package ir.plantcare.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ir.plantcare.app.data.Plant
import ir.plantcare.app.util.ImageUtils
import ir.plantcare.app.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    plants: List<Plant>,
    onAddPlant: () -> Unit,
    onOpenPlant: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("گیاه‌یار 🌿") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "تنظیمات")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlant) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن گیاه")
            }
        }
    ) { padding ->
        if (plants.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("هنوز گیاهی ثبت نکرده‌اید. با دکمه + شروع کنید 🌱")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(plants) { plant ->
                    PlantCard(plant = plant, onClick = { onOpenPlant(plant.id) })
                }
            }
        }
    }
}

@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoFile = ImageUtils.fileFor(context, plant.photoFileName)

    val now = System.currentTimeMillis()
    val nextWatering = plant.nextWateringMillis()
    val isDue = nextWatering != null && nextWatering <= now

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = plant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🪴", style = MaterialTheme.typography.displayMedium)
                    }
                }
                if (isDue) {
                    Icon(
                        Icons.Filled.WaterDrop,
                        contentDescription = "نیاز به آب",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    )
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(plant.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (plant.species.isNotBlank()) {
                    Text(plant.species, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                val wateringText = nextWatering?.let {
                    if (it <= now) "امروز نیاز به آب دارد" else "آبیاری بعدی: ${JalaliCalendar.format(it)}"
                } ?: "هنوز آبیاری ثبت نشده"
                Text(
                    wateringText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
