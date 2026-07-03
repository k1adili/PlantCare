package ir.plantcare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.plantcare.app.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val baseTypography = Typography()

val PlantCareTypography = Typography(
    displayLarge = baseTypography.displayLarge.copy(fontFamily = Vazirmatn),
    displayMedium = baseTypography.displayMedium.copy(fontFamily = Vazirmatn),
    displaySmall = baseTypography.displaySmall.copy(fontFamily = Vazirmatn),
    headlineLarge = baseTypography.headlineLarge.copy(fontFamily = Vazirmatn),
    headlineMedium = baseTypography.headlineMedium.copy(fontFamily = Vazirmatn),
    headlineSmall = baseTypography.headlineSmall.copy(fontFamily = Vazirmatn),
    titleLarge = baseTypography.titleLarge.copy(fontFamily = Vazirmatn),
    titleMedium = baseTypography.titleMedium.copy(fontFamily = Vazirmatn),
    titleSmall = baseTypography.titleSmall.copy(fontFamily = Vazirmatn),
    bodyLarge = baseTypography.bodyLarge.copy(fontFamily = Vazirmatn),
    bodyMedium = baseTypography.bodyMedium.copy(fontFamily = Vazirmatn),
    bodySmall = baseTypography.bodySmall.copy(fontFamily = Vazirmatn),
    labelLarge = baseTypography.labelLarge.copy(fontFamily = Vazirmatn),
    labelMedium = baseTypography.labelMedium.copy(fontFamily = Vazirmatn),
    labelSmall = baseTypography.labelSmall.copy(fontFamily = Vazirmatn)
)

val GreenPrimary = Color(0xFF2E7D32)
val GreenSecondary = Color(0xFF81C784)
val EarthBrown = Color(0xFF6D4C41)
val BackgroundLight = Color(0xFFF4F7F2)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = EarthBrown,
    background = BackgroundLight
)

private val DarkColors = darkColorScheme(
    primary = GreenSecondary,
    secondary = GreenPrimary,
    tertiary = EarthBrown
)

@Composable
fun PlantCareTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = PlantCareTypography, content = content)
}
