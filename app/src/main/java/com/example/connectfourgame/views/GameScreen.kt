package com.example.connectfourgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.connectfourgame.model.GameMode

@Composable
fun GameScreen(
    board: Array<IntArray>,
    playerTurn: Boolean,
    winner: Int,
    gameMode: GameMode?,
    onlineGameId: String?,
    isCreatingGame: Boolean,
    currentOnlineGameStatus: String?,
    onColumnClick: (Int) -> Unit,
    onResetGame: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mensaje de estado del juego
        Text(
            text = when {
                winner == 1 -> if (gameMode == GameMode.ONLINE && !isCreatingGame) "¡Tú ganas!" else "¡Jugador 1 gana!"
                winner == 2 -> if (gameMode == GameMode.VS_AI) "¡La IA gana!" else if (gameMode == GameMode.ONLINE && isCreatingGame) "¡Jugador 2 gana!" else "¡Tú ganas!"
                winner == 3 -> "¡Empate!"
                else -> when (gameMode) {
                    GameMode.VS_AI -> if (playerTurn) "Tu turno" else "Turno de la IA..."
                    GameMode.ONLINE -> if (currentOnlineGameStatus == "waiting" && isCreatingGame) "Esperando jugador..." else if (playerTurn) "Tu turno (Online)" else "Turno del oponente (Online)..."
                    else -> if (playerTurn) "Turno del Jugador 1" else "Turno del Jugador 2"
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Lógica para mostrar ID y estado de espera en partida online para el creador
        // **AQUÍ ESTÁ EL CAMBIO CLAVE:** Solo se muestra si el status es "waiting"
        if (gameMode == GameMode.ONLINE && winner == 0 && currentOnlineGameStatus == "waiting" && isCreatingGame && onlineGameId != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Tu ID de partida:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = onlineGameId ?: "Cargando...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esperando a otro jugador...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Yellow
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else if (gameMode == GameMode.ONLINE && winner == 0 && currentOnlineGameStatus == "playing") {
            // Si la partida es online y está en juego, añade un espaciador para mantener el layout consistente
            Spacer(modifier = Modifier.height(16.dp))
        }


        // UI del tablero de juego
        Board(board) { col ->
            onColumnClick(col) // Pasa el evento de click al ViewModel
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de acción
        Button(onClick = onResetGame) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar")
            Text(text = if (gameMode == GameMode.ONLINE) "Salir de Partida Online" else "Reiniciar Juego")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBackToMenu) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Menú principal")
            Text(text = "Volver al Menú Principal")
        }
    }
}