package com.example.connectfourgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameOptionsScreen(
    onBackToMenu: () -> Unit,
    onCreateGame: () -> Unit,
    onJoinGame: (String) -> Unit
) {
    var gameIdInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Juego Online", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onCreateGame) {
            Text("Crear Nueva Partida")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "O", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = gameIdInput,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    gameIdInput = newValue
                }
            },
            label = { Text("Introduce ID de partida (6 cifras)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (gameIdInput.length == 6 && gameIdInput.all { it.isDigit() }) {
                    onJoinGame(gameIdInput)
                }
            },
            enabled = gameIdInput.length == 6 && gameIdInput.all { it.isDigit() }
        ) {
            Text("Unirse a Partida")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBackToMenu) {
            Text("Volver al Menú Principal")
        }
    }
}