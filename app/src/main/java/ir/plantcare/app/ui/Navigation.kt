package ir.plantcare.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.plantcare.app.data.Plant

private object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val EDIT = "edit/{plantId}"
    const val DETAIL = "detail/{plantId}"
    const val SETTINGS = "settings"
    fun edit(id: Long) = "edit/$id"
    fun detail(id: Long) = "detail/$id"
}

@Composable
fun PlantCareNavHost() {
    val navController = rememberNavController()
    val viewModel: PlantViewModel = viewModel()
    val plants by viewModel.plants.collectAsState(initial = emptyList())

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            PlantListScreen(
                plants = plants,
                onAddPlant = { navController.navigate(Routes.ADD) },
                onOpenPlant = { id -> navController.navigate(Routes.detail(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.ADD) {
            AddEditPlantScreen(
                existingPlant = null,
                onBack = { navController.popBackStack() },
                onSave = { plant ->
                    viewModel.addOrUpdatePlant(plant) { navController.popBackStack() }
                },
                onDelete = {}
            )
        }
        composable(
            Routes.EDIT,
            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: 0L
            val plant by viewModel.plantById(plantId).collectAsState(initial = null)
            plant?.let { p ->
                AddEditPlantScreen(
                    existingPlant = p,
                    onBack = { navController.popBackStack() },
                    onSave = { updated ->
                        viewModel.addOrUpdatePlant(updated) { navController.popBackStack() }
                    },
                    onDelete = { toDelete ->
                        viewModel.deletePlant(toDelete)
                        navController.popBackStack(Routes.LIST, false)
                    }
                )
            }
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong("plantId") ?: 0L
            val plant by viewModel.plantById(plantId).collectAsState(initial = null)
            val logs by viewModel.logsForPlant(plantId).collectAsState(initial = emptyList())
            plant?.let { p ->
                PlantDetailScreen(
                    plant = p,
                    logs = logs,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.edit(plantId)) },
                    onLogCare = { type, date, note -> viewModel.logCare(p, type, date, note) },
                    onDeleteLog = { log -> viewModel.deleteLog(log) }
                )
            }
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onExport = { uri, callback -> viewModel.exportBackup(uri, callback) },
                onImport = { uri, callback -> viewModel.importBackup(uri, callback) }
            )
        }
    }
}
