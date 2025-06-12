package com.example.connectfourgame.views.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD32F2F),   // Rojo oscuro para el primario (podría ser el color de énfasis)
    secondary = Color(0xFF1E88E5), // Azul oscuro para el secundario (otro color de énfasis)
    tertiary = Color(0xFF424242),  // Gris oscuro para un tercer color (acentos)
    background = Color(0xFF212121), // Fondo muy oscuro
    surface = Color(0xFF424242),   // Superficies oscuras (tarjetas, paneles)
    onPrimary = Color.White,       // Texto sobre el primario
    onSecondary = Color.White,     // Texto sobre el secundario
    onTertiary = Color.White,      // Texto sobre el terciario
    onBackground = Color.White,    // Texto sobre el fondo oscuro
    onSurface = Color.White        // Texto sobre las superficies oscuras
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ConnectFourGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val currentWindow = (view.context as Activity).window
            currentWindow.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(currentWindow, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}