package com.example.connectfourgame.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.connectfourgame.model.GameMode
import com.example.connectfourgame.ui.GameScreen
import com.example.connectfourgame.ui.MainMenuScreen
import com.example.connectfourgame.ui.OnlineGameOptionsScreen
import com.example.connectfourgame.viewmodel.GameViewModel


@Composable
fun ConnectFourApp(gameViewModel: GameViewModel = viewModel()) {
    // Observar los estados del ViewModel
    val gameMode by gameViewModel.gameMode.collectAsState()
    val board by gameViewModel.board.collectAsState()
    val playerTurn by gameViewModel.playerTurn.collectAsState()
    val winner by gameViewModel.winner.collectAsState()
    val onlineGameId by gameViewModel.onlineGameId.collectAsState()
    val isCreatingGame by gameViewModel.isCreatingGame.collectAsState()
    val currentOnlineGameStatus by gameViewModel.currentOnlineGameStatus.collectAsState()

    // LaunchedEffect para configurar el listener de Firebase cuando onlineGameId cambie
    // o al entrar por primera vez si ya hay un onlineGameId
    LaunchedEffect(onlineGameId) {
        gameViewModel.setupOnlineGameListener(onlineGameId)
    }


    when (gameMode) {
        null -> {
            // Pantalla de menú principal
            MainMenuScreen(
                onTwoPlayersClick = { gameViewModel.setGameMode(GameMode.TWO_PLAYERS) },
                onVsAIClick = { gameViewModel.setGameMode(GameMode.VS_AI) },
                onOnlineClick = { gameViewModel.setGameMode(GameMode.ONLINE) }
            )
        }
        GameMode.ONLINE -> {
            if (onlineGameId == null) {
                // Pantalla de opciones de juego online (Crear/Unirse)
                OnlineGameOptionsScreen(
                    onBackToMenu = { gameViewModel.setGameMode(null) }, // Volver al menú principal
                    onCreateGame = { gameViewModel.createOnlineGame() },
                    onJoinGame = { gameId -> gameViewModel.joinOnlineGame(gameId) }
                )
            } else {
                // Pantalla de juego online (una vez que se ha creado o unido)
                GameScreen(
                    board = board,
                    playerTurn = playerTurn,
                    winner = winner,
                    gameMode = gameMode,
                    onlineGameId = onlineGameId,
                    isCreatingGame = isCreatingGame,
                    currentOnlineGameStatus = currentOnlineGameStatus,
                    onColumnClick = { col -> gameViewModel.dropDisc(col) },
                    onResetGame = { gameViewModel.resetGame() },
                    onBackToMenu = { gameViewModel.setGameMode(null) }
                )
            }
        }
        else -> { // GameMode.TWO_PLAYERS o GameMode.VS_AI
            // Pantalla de juego para modos offline
            GameScreen(
                board = board,
                playerTurn = playerTurn,
                winner = winner,
                gameMode = gameMode,
                onlineGameId = onlineGameId, // Será null o irrelevante para offline
                isCreatingGame = isCreatingGame, // Será false o irrelevante para offline
                currentOnlineGameStatus = currentOnlineGameStatus, // Será null o irrelevante para offline
                onColumnClick = { col -> gameViewModel.dropDisc(col) },
                onResetGame = { gameViewModel.resetGame() },
                onBackToMenu = { gameViewModel.setGameMode(null) }
            )
        }
    }
}