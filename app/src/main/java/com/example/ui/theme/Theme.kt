package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StudyGuardColorScheme = darkColorScheme(
    primary = ShieldCyan,
    onPrimary = DarkBg,
    primaryContainer = SurfaceCardElevated,
    onPrimaryContainer = ShieldCyan,
    secondary = StrictEmerald,
    onSecondary = DarkBg,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = StrictEmerald,
    tertiary = WarningAmber,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error = AlarmCrimson,
    onError = TextPrimary,
    errorContainer = AlarmCrimsonBg,
    onErrorContainer = AlarmCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Forced bold security palette
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBg.toArgb()
                window.navigationBarColor = DarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = StudyGuardColorScheme,
        typography = Typography,
        content = content
    )
}
