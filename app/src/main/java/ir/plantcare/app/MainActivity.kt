package ir.plantcare.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.plantcare.app.ui.PlantCareNavHost
import ir.plantcare.app.ui.theme.PlantCareTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            // اپ فقط فارسی است، پس صرف‌نظر از زبان سیستم همیشه راست‌به‌چپ نمایش داده می‌شود
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                PlantCareTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        PlantCareNavHost()
                    }
                }
            }
        }
    }
}
