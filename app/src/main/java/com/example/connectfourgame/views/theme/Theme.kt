package com.example.connectfourgame.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.connectfourgame.views.theme.Pink40
import com.example.connectfourgame.views.theme.Pink80
import com.example.connectfourgame.views.theme.Purple40
import com.example.connectfourgame.views.theme.Purple80
import com.example.connectfourgame.views.theme.PurpleGrey40
import com.example.connectfourgame.views.theme.PurpleGrey80

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
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
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // --- CAMBIOS CLAVE AQUÍ ---
            // 1. Establece el color de la barra de estado a negro
            window.statusBarColor = Color.Black.toArgb() // <-- Usamos Color.Black de Compose UI

            // 2. Controla si los iconos de la barra de estado son claros u oscuros
            //    Si el fondo es negro, queremos iconos blancos/claros para que se vean.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            // isAppearanceLightStatusBars = false significa que los iconos serán oscuros sobre un fondo CLARO.
            // Si tu barra de estado es negra, necesitas que los iconos sean CLAROS, así que 'false' es lo correcto.
            // Si tu barra de estado fuera clara, querrías 'true' para iconos oscuros.
            // --- FIN CAMBIOS CLAVE ---

            // Esta línea la mantienes como antes para que tu contenido pueda dibujarse
            // debajo de las barras del sistema si decides que la imagen las cubra.
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}