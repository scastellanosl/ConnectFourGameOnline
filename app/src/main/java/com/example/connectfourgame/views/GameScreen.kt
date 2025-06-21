package com.example.connectfourgame.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connectfourgame.R
import com.example.connectfourgame.model.GameMode
import com.example.connectfourgame.viewmodel.GameViewModel
import com.example.connectfourgame.views.composables.Board
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign

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
    onBackToMenu: () -> Unit,
    viewModel: GameViewModel? = null
) {

    // 1. Mostrar código de sala SOLO si eres anfitrión y la sala está esperando
    if (
        gameMode == GameMode.ONLINE &&
        winner == 0 &&
        currentOnlineGameStatus == "waiting" &&
        isCreatingGame &&
        onlineGameId != null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.background_online),
                contentDescription = "Fondo del Menú Online",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.35f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Tu ID de partida:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = onlineGameId,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esperando a otro jugador...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(top = 20.dp),
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        return
    }

    // 2. Mostrar pregunta SOLO si la partida está en juego y es tu turno
    if (
        gameMode == GameMode.ONLINE &&
        playerTurn &&
        viewModel != null &&
        currentOnlineGameStatus == "playing"
    ) {
        val currentWord by viewModel.currentWordEnglish.collectAsState()
        val questionAttempted by viewModel.questionAttempted.collectAsState()
        val secondsLeft by viewModel.secondsLeft.collectAsState()

        LaunchedEffect(playerTurn, currentWord, questionAttempted, currentOnlineGameStatus) {
            if (!questionAttempted && currentWord.isBlank() && onlineGameId != null && currentOnlineGameStatus == "playing") {
                viewModel.assignWordAndStartTimer(onlineGameId)
            }
        }

        if (!questionAttempted && currentWord.isNotBlank()) {
            VocabularyQuestion(
                wordEnglish = currentWord,
                secondsLeft = secondsLeft,
                onSubmit = { answer ->
                    viewModel.submitTranslationAnswer(onlineGameId.toString(), answer)
                }
            )
            return
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo de pantalla con imagen
        Image(
            painter = painterResource(id = R.drawable.background_game),
            contentDescription = "Fondo de Pantalla del Juego",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Botón de flecha para volver al menú principal en la esquina superior izquierda
        IconButton(
            onClick = onBackToMenu,
            modifier = Modifier
                .align(Alignment.TopStart) // Alinea el botón en la esquina superior izquierda de la Box
                .systemBarsPadding() // <<< ¡AQUÍ ESTÁ EL CAMBIO! Agrega padding para las barras del sistema.
                .padding(16.dp) // Padding adicional desde los bordes seguros de la pantalla
                .size(50.dp) // Tamaño del botón de icono
                .scale(1.5f, 1.5f)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver al Menú Principal",
                tint = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            val canPlay = when {
                gameMode == GameMode.ONLINE -> viewModel?.questionAttempted?.collectAsState()?.value == true &&
                        viewModel?.lastGuessedCorrectly?.collectAsState()?.value == true
                else -> true
            }
            Board(board) { col ->
                if (canPlay) onColumnClick(col)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Button(onClick = onResetGame) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar")
                Text(text = if (gameMode == GameMode.ONLINE) "Salir de Partida Online" else "Reiniciar Juego")
            }
        }
    }
}

// Composable de solicitud de palabra
@Composable
fun VocabularyQuestion(
    wordEnglish: String,
    onSubmit: (String) -> Unit,
    secondsLeft: Int
) {
    var answer by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(id = R.drawable.background_game),
            contentDescription = "Fondo de Pantalla del Juego",
            modifier = Modifier.fillMaxSize().alpha(0.2f),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.align(alignment = Alignment.Center)){

            Text("Traduce la palabra:", fontWeight = FontWeight.Bold, fontSize = 25.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), textAlign = TextAlign.Center)
            Text(wordEnglish, fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontStyle = FontStyle.Italic)
            OutlinedTextField(modifier = Modifier.align(Alignment.CenterHorizontally),
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Traducción en español")}
            )
            Text("Tiempo restante: $secondsLeft s", color = Color.Red, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 20.dp))

            Button(onClick = { onSubmit(answer) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Enviar")
            }

        }

    }
}
