package ru.obroki.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.obroki.app.domain.AppThemeMode

val StatusPaid = Color(0xFF2E7D32)
val StatusOverdue = Color(0xFFC62828)
val StatusSoon = Color(0xFFF9A825)
/** Fallback when a calendar day mixes statuses we cannot segment cleanly. */
val StatusMixedFallback = Color(0xFF1565C0)

val RowPaidBg = Color(0xFFC8E6C9)
val RowOverdueBg = Color(0xFFFFCDD2)
val RowSoonBg = Color(0xFFFFF9C4)

val RowPaidBgDark = Color(0xFF1B5E20)
val RowOverdueBgDark = Color(0xFF7F1D1D)
val RowSoonBgDark = Color(0xFF785A00)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = StatusPaid,
    tertiary = StatusSoon,
    error = StatusOverdue,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFFFFF176),
    error = Color(0xFFEF9A9A),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun ObrokiTheme(
    mode: AppThemeMode,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
