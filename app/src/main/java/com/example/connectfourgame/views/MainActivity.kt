package com.example.connectfourgame.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.connectfourgame.views.theme.ConnectFourGameTheme
/**
 * Actividad principal que aloja el juego Conecta Cuatro usando Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectFourGameTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Llamar al Composable principal de la aplicación
                    ConnectFourApp()
                }
            }
        }
    }
}