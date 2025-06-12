package com.example.connectfourgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.connectfourgame.ui.theme.ConnectFourGameTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

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
                    ConnectFourGame()
                }
            }
        }
    }
}

/**
 * Representa el modo de juego seleccionado por el usuario.
 */
enum class GameMode {
    TWO_PLAYERS, VS_AI, ONLINE
}

/**
 * Representa el estado de una partida online de Conecta Cuatro.
 * Esta clase de datos refleja la estructura que usaremos en Firebase Realtime Database.
 */
data class Game(
    val gameId: String = "",
    val player1Id: String = "", // UID del jugador 1
    val player2Id: String? = null, // UID del jugador 2, puede ser null si está esperando
    // El tablero: 6 filas, 7 columnas. 0 = vacío, 1 = Jugador 1, 2 = Jugador 2
    val board: List<List<Int>> = List(6) { List(7) { 0 } },
    val currentTurnPlayerId: String = "", // UID del jugador cuyo turno es
    val status: String = "waiting", // "waiting", "playing", "finished", "draw"
    val winnerId: String? = null, // UID del ganador, o null si no hay ganador o empate
    val currentWordEnglish: String = "", // Palabra en inglés para la pregunta de vocabulario
    val correctTranslationSpanish: String = "", // Traducción correcta en español
    val lastGuessedCorrectly: Boolean = false, // Indica si la última pregunta fue respondida correctamente
    val questionAttempted: Boolean = false // True si la pregunta ya se mostró en este turno y está esperando respuesta
)

/**
 * Representa una palabra y su traducción para el juego de vocabulario (actualmente no implementado).
 */
data class Word(
    val english: String = "",
    val spanish: String = ""
)


/**
 * Composable raíz que contiene el estado y la UI del juego.
 */
@OptIn(ExperimentalMaterial3Api::class) // Necesario para OutlinedTextField
@Composable
fun ConnectFourGame() {
    val rows = 6
    val cols = 7

    // Referencia a la base de datos de Firebase
    val database = remember { Firebase.database.reference }

    var board by remember { mutableStateOf(Array(rows) { IntArray(cols) { 0 } }) }
    var playerTurn by remember { mutableStateOf(true) } // true para Jugador 1 (local o tu turno en online)
    var winner by remember { mutableStateOf(0) } // 0=none, 1=P1, 2=P2, 3=Draw
    var gameMode by remember { mutableStateOf<GameMode?>(null) }

    // Variables para el juego online
    var onlineGameId by remember { mutableStateOf<String?>(null) } // El ID de la partida a la que se une/crea
    var isCreatingGame by remember { mutableStateOf(false) } // true si está creando una partida, false si se une
    val playerLocalId = remember { UUID.randomUUID().toString() }

    // Estado para mostrar el Game ID en un diálogo -- ELIMINADAS ESTAS VARIABLES, EL DIÁLOGO YA NO SE USA
    // var showGameIdDialog by remember { mutableStateOf(false) }
    // var displayedGameId by remember { mutableStateOf("") }

    // Referencia al juego online activo en Firebase para el listener
    var onlineGameRef: DatabaseReference? by remember { mutableStateOf(null) }
    // Listener que observará los cambios en la partida online
    var gameEventListener: ValueEventListener? by remember { mutableStateOf(null) }

    // Estado local para el status de la partida online (principalmente para el creador)
    var currentOnlineGameStatus by remember(onlineGameId) { mutableStateOf<String?>(null) }


    // --- Efecto para gestionar el listener de la partida online ---
    DisposableEffect(onlineGameId) {
        // Capturamos el onlineGameId actual para usarlo dentro del onDispose
        val currentOnlineGameId = onlineGameId

        // Limpiamos los listeners y referencias globales al inicio para seguridad,
        // ya que este DisposableEffect se re-ejecuta cuando onlineGameId cambia.
        var listenerToDispose: ValueEventListener? = null
        var refToDispose: DatabaseReference? = null

        if (currentOnlineGameId == null) {
            // Si no hay un ID de partida online, aseguramos que las referencias globales sean null
            onlineGameRef = null
            gameEventListener = null
            currentOnlineGameStatus = null // Reseteamos el estado del status también
            onDispose {
                println("DisposableEffect dispuesto sin listener activo.")
            }
        } else {
            // Si hay un ID de partida online, configuramos el listener
            // **FIXED LINE HERE**
            val gameRef = database.child("games").child(currentOnlineGameId) // Changed 'currentOnlineId' to 'currentOnlineGameId'
            refToDispose = gameRef // Guarda la referencia local para el onDispose

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val game = snapshot.getValue(Game::class.java)
                    if (game != null) {
                        board = game.board.toTypedArrayOfIntArray()
                        playerTurn = (game.currentTurnPlayerId == playerLocalId)
                        currentOnlineGameStatus = game.status // Actualiza el estado del status

                        // Determina el ganador o empate
                        // **FIX**: Solo actualiza 'winner' si la partida ha terminado o es empate
                        winner = if (game.status == "finished") {
                            when (game.winnerId) {
                                game.player1Id -> 1
                                game.player2Id -> 2
                                else -> 0 // Si el status es finished pero winnerId no coincide (debería ser handled en Firebase)
                            }
                        } else if (game.status == "draw") {
                            3 // Es un empate
                        } else {
                            0 // La partida sigue en curso o esperando
                        }

                        println("Estado de la partida actualizado desde Firebase:")
                        println("  ID: ${game.gameId}")
                        println("  Turno de: ${game.currentTurnPlayerId == playerLocalId} (Yo: $playerLocalId)")
                        println("  Estado: ${game.status}")
                        println("  Ganador: ${game.winnerId ?: "Ninguno"}")
                    } else {
                        println("La partida $currentOnlineGameId ya no existe en Firebase. Volviendo al menú.")
                        onlineGameId = null // Esto activará una nueva disposición del efecto
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error al leer la partida online: ${error.message}")
                    // TODO: Podrías mostrar un mensaje de error al usuario
                }
            }
            listenerToDispose = listener // Guarda el listener local para el onDispose

            gameRef.addValueEventListener(listener) // Adjunta el listener a la referencia de Firebase

            // Actualizamos las variables de estado globales con las referencias del listener activo
            onlineGameRef = gameRef
            gameEventListener = listener

            println("Listener de Firebase ADJUNTO para partida $currentOnlineGameId")

            // Este es el bloque de limpieza que se ejecutará cuando el efecto se disponga
            // (es decir, cuando 'currentOnlineGameId' cambie o el composable se retire).
            onDispose {
                refToDispose?.removeEventListener(listenerToDispose ?: return@onDispose)
                println("Listener de Firebase REMOVIDO para la partida $currentOnlineGameId.")
                // Resetear las variables de estado globales a null después de la limpieza
                onlineGameRef = null
                gameEventListener = null
                currentOnlineGameStatus = null // Asegurarse de que el status también se resetea
            }
        }
    }


    // --- PANTALLA DE SELECCIÓN DE MODO DE JUEGO ---
    if (gameMode == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = { gameMode = GameMode.TWO_PLAYERS }) {
                Text("Jugar Local (2 Jugadores)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { gameMode = GameMode.VS_AI }) {
                Text("Jugar contra la IA")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { gameMode = GameMode.ONLINE }) {
                Text("Jugar Online")
            }
        }
        return
    }

    // --- PANTALLA DE OPCIONES DE JUEGO ONLINE ---
    if (gameMode == GameMode.ONLINE && onlineGameId == null) {
        OnlineGameOptions(
            onBackToMenu = { gameMode = null },
            onCreateGame = {
                generateUniqueGameId(database) { newGameId ->
                    if (newGameId != null) {
                        val initialBoard = List(rows) { List(cols) { 0 } }
                        val newGame = Game(
                            gameId = newGameId,
                            player1Id = playerLocalId,
                            player2Id = null,
                            board = initialBoard,
                            currentTurnPlayerId = playerLocalId,
                            status = "waiting", // Initial status is "waiting"
                            winnerId = null
                        )

                        database.child("games").child(newGameId).setValue(newGame)
                            .addOnSuccessListener {
                                onlineGameId = newGameId // This triggers the game screen to show
                                isCreatingGame = true // Mark as creator
                                board = initialBoard.toTypedArrayOfIntArray() // Reset board visually
                                winner = 0 // No winner yet
                                playerTurn = true // Creator's turn initially (waiting)
                                println("Partida online creada con ID: $newGameId. Esperando jugador 2...")
                                // **FIX**: Removed displayedGameId and showGameIdDialog lines
                            }
                            .addOnFailureListener { e ->
                                println("Error al crear partida online: ${e.message}")
                            }
                    } else {
                        println("No se pudo crear la partida: No se encontró un ID único.")
                    }
                }
            },
            onJoinGame = { enteredGameId ->
                if (enteredGameId.length == 6 && enteredGameId.all { it.isDigit() }) {
                    val gameRef = database.child("games").child(enteredGameId)

                    gameRef.get().addOnSuccessListener { dataSnapshot ->
                        val existingGame = dataSnapshot.getValue(Game::class.java)

                        if (existingGame != null) {
                            if (existingGame.status == "waiting" && existingGame.player2Id == null) {
                                val updatedGame = existingGame.copy(
                                    player2Id = playerLocalId,
                                    status = "playing" // Change status to "playing" when player 2 joins
                                )

                                gameRef.setValue(updatedGame)
                                    .addOnSuccessListener {
                                        onlineGameId = enteredGameId
                                        isCreatingGame = false // Not the creator
                                        println("Te has unido a la partida con ID: $enteredGameId")
                                        // The listener on the creator's side will detect the status change to "playing"
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error al unirse a la partida: ${e.message}")
                                    }
                            } else {
                                println("La partida no está disponible para unirse.")
                            }
                        } else {
                            println("La partida con ID $enteredGameId no existe.")
                        }
                    }.addOnFailureListener { e ->
                        println("Error al obtener partida: ${e.message}")
                    }
                } else {
                    println("ERROR: El ID de partida debe ser un número de 6 cifras.")
                }
            }
        )
        return
    }


    // --- LAYOUT PRINCIPAL DEL JUEGO (Local, AI, o Online una vez que onlineGameId no sea null) ---
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Mensaje de estado del juego
        Text(
            text = when {
                winner == 1 -> if (gameMode == GameMode.ONLINE && !isCreatingGame) "¡Tú ganas!" else "¡Jugador 1 gana!"
                winner == 2 -> if (gameMode == GameMode.VS_AI) "¡La IA gana!" else if (gameMode == GameMode.ONLINE && isCreatingGame) "¡Jugador 2 gana!" else "¡Tú ganas!"
                winner == 3 -> "¡Empate!"
                else -> when (gameMode) {
                    GameMode.VS_AI -> if (playerTurn) "Tu turno" else "Turno de la IA..."
                    // **FIX**: Show "Waiting" message based on currentOnlineGameStatus
                    GameMode.ONLINE -> if (currentOnlineGameStatus == "waiting" && isCreatingGame) "Esperando jugador..." else if (playerTurn) "Tu turno (Online)" else "Turno del oponente (Online)..."
                    else -> if (playerTurn) "Turno del Jugador 1" else "Turno del Jugador 2"
                }
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // **FIX**: Lógica para mostrar ID y estado de espera en partida online para el creador
        if (gameMode == GameMode.ONLINE && winner == 0) { // Solo si es online y no hay ganador aún
            if (isCreatingGame && onlineGameId != null) {
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
                        text = if (currentOnlineGameStatus == "playing") "¡Partida en curso!" else "Esperando a otro jugador...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (currentOnlineGameStatus == "playing") Color.Green else Color.Yellow
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (!isCreatingGame && onlineGameId != null && currentOnlineGameStatus == "playing") {
                // If you joined a game and it's playing, just maintain consistent spacing
                Spacer(modifier = Modifier.height(16.dp))
            }
        }


        // UI del tablero de juego
        Board(board) { col ->
            if (winner == 0) {
                when (gameMode) {
                    GameMode.TWO_PLAYERS -> {
                        val row = findAvailableRow(board, col)
                        if (row != -1) {
                            val currentPlayer = if (playerTurn) 1 else 2
                            board = board.copyWithMove(row, col, currentPlayer)
                            when {
                                checkWinner(board, currentPlayer) -> winner = currentPlayer
                                isBoardFull(board) -> winner = 3
                                else -> playerTurn = !playerTurn
                            }
                        }
                    }
                    GameMode.VS_AI -> {
                        if (playerTurn) {
                            val row = findAvailableRow(board, col)
                            if (row != -1) {
                                val currentPlayer = 1
                                board = board.copyWithMove(row, col, currentPlayer)
                                when {
                                    checkWinner(board, currentPlayer) -> winner = currentPlayer
                                    isBoardFull(board) -> winner = 3
                                    else -> playerTurn = !playerTurn
                                }
                            }
                        }
                    }
                    GameMode.ONLINE -> {
                        // **FIX**: Only allow moves if currentOnlineGameStatus is "playing"
                        if (playerTurn && onlineGameId != null && onlineGameRef != null && currentOnlineGameStatus == "playing") {
                            val row = findAvailableRow(board, col)
                            if (row != -1) {
                                onlineGameRef?.get()?.addOnSuccessListener { dataSnapshot ->
                                    val currentOnlineGame = dataSnapshot.getValue(Game::class.java)
                                    if (currentOnlineGame != null && currentOnlineGame.currentTurnPlayerId == playerLocalId) {
                                        val currentPlayerValue = if (currentOnlineGame.player1Id == playerLocalId) 1 else 2
                                        val nextPlayerId = if (currentOnlineGame.player1Id == playerLocalId) {
                                            currentOnlineGame.player2Id
                                        } else {
                                            currentOnlineGame.player1Id
                                        }

                                        val updatedBoardList = currentOnlineGame.board.toMutableList()
                                        val newRow = updatedBoardList[row].toMutableList()
                                        newRow[col] = currentPlayerValue
                                        updatedBoardList[row] = newRow.toList()

                                        val tempBoardArray = updatedBoardList.toTypedArrayOfIntArray()
                                        val newWinnerValue = when {
                                            checkWinner(tempBoardArray, currentPlayerValue) -> currentPlayerValue
                                            isBoardFull(tempBoardArray) -> 3
                                            else -> 0
                                        }

                                        val updatedGame = currentOnlineGame.copy(
                                            board = updatedBoardList,
                                            currentTurnPlayerId = nextPlayerId ?: "",
                                            status = if (newWinnerValue != 0) "finished" else "playing",
                                            winnerId = when (newWinnerValue) {
                                                1 -> currentOnlineGame.player1Id
                                                2 -> currentOnlineGame.player2Id
                                                else -> null
                                            }
                                        )

                                        onlineGameRef?.setValue(updatedGame)
                                            ?.addOnSuccessListener {
                                                println("Movimiento enviado a Firebase para la columna $col")
                                            }
                                            ?.addOnFailureListener { e ->
                                                println("Error al enviar movimiento a Firebase: ${e.message}")
                                            }
                                    } else {
                                        println("No es tu turno o la partida ha cambiado mientras intentabas mover.")
                                    }
                                }?.addOnFailureListener { e ->
                                    println("Error al obtener datos de partida para movimiento: ${e.message}")
                                }

                            } else {
                                println("Columna $col está llena.")
                            }
                        } else if (currentOnlineGameStatus != "playing") { // **FIX**: More specific message for waiting state
                            println("La partida online aún no ha comenzado. Esperando al otro jugador.")
                        } else if (!playerTurn) {
                            println("No es tu turno en la partida online.")
                        } else {
                            println("Partida online no iniciada o ID nulo.")
                        }
                    }
                    null -> { /* Should not happen if the return statements are correct */ }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Reiniciar juego / Salir de partida online
        Button(onClick = {
            board = Array(rows) { IntArray(cols) { 0 } }
            winner = 0
            playerTurn = true
            if (gameMode == GameMode.ONLINE) {
                onlineGameRef?.removeEventListener(gameEventListener ?: return@Button)
                onlineGameRef = null
                gameEventListener = null
                onlineGameId = null // Go back to online options screen
                currentOnlineGameStatus = null // Reset status
            }
        }) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar")
        }

        // Botón Volver al menú principal
        Button(onClick = {
            gameMode = null
            if (onlineGameId != null) { // If in an online game, clean up listeners
                onlineGameRef?.removeEventListener(gameEventListener ?: return@Button)
                onlineGameRef = null
                gameEventListener = null
                onlineGameId = null
                currentOnlineGameStatus = null // Reset status
            }
        }) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Menú principal")
        }

    } // Fin del Column principal del juego

    // **FIX**: REMOVED the AlertDialog for displaying Game ID.
    // It's no longer needed as the ID is displayed directly on the game screen for the creator.
}

/**
 * Nuevo Composable para las opciones de juego online: crear o unirse a una partida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineGameOptions(
    onBackToMenu: () -> Unit,
    onCreateGame: () -> Unit,
    onJoinGame: (String) -> Unit
) {
    var gameIdInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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


/**
 * Dibuja la cuadrícula completa del tablero con columnas clicables.
 */
@Composable
fun Board(board: Array<IntArray>, onColumnClick: (Int) -> Unit) {
    Column {
        for (row in board) {
            Row {
                for ((colIndex, cell) in row.withIndex()) {
                    Cell(cell) { onColumnClick(colIndex) }
                }
            }
        }
    }
}

/**
 * Una sola celda del tablero de juego.
 */
@Composable
fun Cell(value: Int, onClick: () -> Unit) {
    val color = when (value) {
        1 -> Color.Red
        2 -> Color.Blue
        // **FIX**: Cambiado de LightGray a Gray para mejor visibilidad en tema oscuro
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)
            .background(color, CircleShape)
            .clickable { onClick() }
    )
}

/**
 * Encuentra la siguiente fila disponible en una columna de abajo hacia arriba.
 */
fun findAvailableRow(board: Array<IntArray>, col: Int): Int {
    for (r in board.indices.reversed()) {
        if (board[r][col] == 0) return r
    }
    return -1
}

/**
 * Comprueba si hay cuatro piezas conectadas horizontal, vertical o diagonalmente.
 */
fun checkWinner(board: Array<IntArray>, player: Int): Boolean {
    val rows = board.size
    val cols = board[0].size

    // Comprobar horizontal
    for (r in 0 until rows) {
        for (c in 0..cols - 4) {
            if (board[r][c] == player &&
                board[r][c + 1] == player &&
                board[r][c + 2] == player &&
                board[r][c + 3] == player
            ) {
                return true
            }
        }
    }

    // Comprobar vertical
    for (c in 0 until cols) {
        for (r in 0..rows - 4) {
            if (board[r][c] == player &&
                board[r + 1][c] == player &&
                board[r + 2][c] == player &&
                board[r + 3][c] == player
            ) {
                return true
            }
        }
    }

    // Comprobar diagonal (arriba-izquierda a abajo-derecha)
    for (r in 0..rows - 4) {
        for (c in 0..cols - 4) {
            if (board[r][c] == player &&
                board[r + 1][c + 1] == player &&
                board[r + 2][c + 2] == player &&
                board[r + 3][c + 3] == player
            ) {
                return true
            }
        }
    }

    // Comprobar diagonal (abajo-izquierda a arriba-derecha)
    for (r in 3 until rows) { // Empezar desde la fila 3 (índice 3) para subir
        for (c in 0..cols - 4) {
            if (board[r][c] == player &&
                board[r - 1][c + 1] == player &&
                board[r - 2][c + 2] == player &&
                board[r - 3][c + 3] == player
            ) {
                return true
            }
        }
    }

    return false
}

/**
 * Devuelve true si el tablero no tiene celdas vacías.
 */
fun isBoardFull(board: Array<IntArray>): Boolean {
    return board.all { row -> row.all { it != 0 } }
}

/**
 * Crea una copia del tablero con un movimiento aplicado en [row], [col] por [player].
 */
fun Array<IntArray>.copyWithMove(row: Int, col: Int, player: Int): Array<IntArray> {
    return Array(size) { r ->
        IntArray(this[0].size) { c ->
            if (r == row && c == col) player else this[r][c]
        }
    }
}

/**
 * Función de extensión para convertir List<List<Int>> (usado en la clase de datos Game)
 * a Array<IntArray> (usado en el estado local del board).
 */
fun List<List<Int>>.toTypedArrayOfIntArray(): Array<IntArray> {
    return Array(size) { rowIndex ->
        IntArray(this[rowIndex].size) { colIndex ->
            this[rowIndex][colIndex]
        }
    }
}

/**
 * Genera un ID de partida numérico único de 6 cifras y lo verifica en Firebase.
 *
 * @param database La referencia a la Realtime Database.
 * @param maxRetries El número máximo de intentos para encontrar un ID único.
 * @param callback Un lambda que recibe el ID único encontrado (o null si falla).
 */
fun generateUniqueGameId(database: DatabaseReference, maxRetries: Int = 10, callback: (String?) -> Unit) {
    if (maxRetries <= 0) {
        println("ERROR: No se pudo generar un ID de partida único después de varios intentos.")
        callback(null) // No se pudo encontrar un ID único
        return
    }

    val newId = (100000..999999).random().toString()

    database.child("games").child(newId).get().addOnSuccessListener { dataSnapshot ->
        if (!dataSnapshot.exists()) {
            callback(newId)
        } else {
            println("ID '$newId' ya existe, reintentando... (intentos restantes: ${maxRetries - 1})")
            generateUniqueGameId(database, maxRetries - 1, callback)
        }
    }.addOnFailureListener { e ->
        println("ERROR al verificar la unicidad del ID de partida: ${e.message}")
        callback(null)
    }
}