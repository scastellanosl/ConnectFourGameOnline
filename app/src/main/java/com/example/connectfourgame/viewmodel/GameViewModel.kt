package com.example.connectfourgame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.connectfourgame.model.Game
import com.example.connectfourgame.model.GameMode
import com.example.connectfourgame.utils.checkWinner
import com.example.connectfourgame.utils.copyWithMove
import com.example.connectfourgame.utils.findAvailableRow
import com.example.connectfourgame.utils.generateUniqueGameId
import com.example.connectfourgame.utils.isBoardFull
import com.example.connectfourgame.utils.toTypedArrayOfIntArray
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel : ViewModel() {

    // --- Propiedades de Estado del Juego (observables por la UI) ---
    private val _board = MutableStateFlow(Array(6) { IntArray(7) { 0 } })
    val board: StateFlow<Array<IntArray>> = _board.asStateFlow()

    private val _playerTurn = MutableStateFlow(true) // true para Jugador 1 (local o tu turno en online)
    val playerTurn: StateFlow<Boolean> = _playerTurn.asStateFlow()

    private val _winner = MutableStateFlow(0) // 0=none, 1=P1, 2=P2, 3=Draw
    val winner: StateFlow<Int> = _winner.asStateFlow()

    private val _gameMode = MutableStateFlow<GameMode?>(null)
    val gameMode: StateFlow<GameMode?> = _gameMode.asStateFlow()

    // --- Propiedades para el Juego Online ---
    private val database: DatabaseReference = Firebase.database.reference
    private val playerLocalId: String = UUID.randomUUID().toString()

    private val _onlineGameId = MutableStateFlow<String?>(null)
    val onlineGameId: StateFlow<String?> = _onlineGameId.asStateFlow()

    private val _isCreatingGame = MutableStateFlow(false) // true si está creando una partida, false si se une
    val isCreatingGame: StateFlow<Boolean> = _isCreatingGame.asStateFlow()

    private val _currentOnlineGameStatus = MutableStateFlow<String?>(null)
    val currentOnlineGameStatus: StateFlow<String?> = _currentOnlineGameStatus.asStateFlow()

    // Referencias para Firebase (gestionadas internamente por el ViewModel)
    private var onlineGameRef: DatabaseReference? = null
    private var gameEventListener: ValueEventListener? = null

    // Estado para la palabra actual en inglés
    private val _currentWordEnglish = MutableStateFlow("")
    val currentWordEnglish: StateFlow<String> = _currentWordEnglish

    // Estado para saber si la pregunta ya fue intentada
    private val _questionAttempted = MutableStateFlow(false)
    val questionAttempted: StateFlow<Boolean> = _questionAttempted

    // Estado para el temporizador
    private val _secondsLeft = MutableStateFlow(15)
    val secondsLeft: StateFlow<Int> = _secondsLeft


    // Cuando recibas datos de Firebase, actualiza estos estados:
    fun updateVocabularyStatesFromGame(game: Game) {
        _currentWordEnglish.value = game.currentWordEnglish
        _questionAttempted.value = game.questionAttempted
        // _secondsLeft.value lo manejas en tu lógica de temporizador
    }

    init {
        resetGame()
    }

    // --- Funciones de Gestión de Listener de Partida Online ---
    fun setupOnlineGameListener(gameId: String?) {
        onlineGameRef?.removeEventListener(gameEventListener ?: return)
        onlineGameRef = null
        gameEventListener = null
        _currentOnlineGameStatus.value = null

        if (gameId == null) {
            println("setupOnlineGameListener: Game ID es nulo. Limpiando listener.")
            _onlineGameId.value = null
            return
        }

        val gameRef = database.child("games").child(gameId)
        onlineGameRef = gameRef

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(Game::class.java)
                if (game != null) {
                    updateVocabularyStatesFromGame(game)
                    _board.value = game.board.toTypedArrayOfIntArray()
                    _playerTurn.value = (game.currentTurnPlayerId == playerLocalId)
                    _currentOnlineGameStatus.value = game.status

                    _winner.value = if (game.status == "finished") {
                        when (game.winnerId) {
                            game.player1Id -> 1
                            game.player2Id -> 2
                            else -> 0
                        }
                    } else if (game.status == "draw") {
                        3
                    } else {
                        0
                    }

                    println("Estado de la partida actualizado desde Firebase:")
                    println("  ID: ${game.gameId}")
                    println("  Turno de: ${game.currentTurnPlayerId == playerLocalId} (Yo: $playerLocalId)")
                    println("  Estado: ${game.status}")
                    println("  Ganador: ${game.winnerId ?: "Ninguno"}")
                } else {
                    println("La partida $gameId ya no existe en Firebase. Volviendo al menú.")
                    resetOnlineGame()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error al leer la partida online: ${error.message}")
            }
        }
        gameEventListener = listener

        gameRef.addValueEventListener(listener)
        println("Listener de Firebase ADJUNTO para partida $gameId")
    }

    override fun onCleared() {
        super.onCleared()
        onlineGameRef?.removeEventListener(gameEventListener ?: return)
        println("ViewModel onCleared: Listener de Firebase REMOVIDO.")
    }

    // --- Funciones de Lógica del Juego ---
    fun setGameMode(mode: GameMode?) {
        _gameMode.value = mode
        resetGame()
    }

    fun dropDisc(col: Int) {
        val currentBoard = _board.value
        val currentWinner = _winner.value
        val currentMode = _gameMode.value

        if (currentWinner != 0) return

        when (currentMode) {
            GameMode.TWO_PLAYERS -> handleLocalMove(col)
            GameMode.VS_AI -> handleAIMove(col)
            GameMode.ONLINE -> handleOnlineMove(col)
            null -> { /* No hacer nada si no hay modo seleccionado */ }
        }
    }

    private fun handleLocalMove(col: Int) {
        val rows = _board.value.size
        val currentPlayerValue = if (_playerTurn.value) 1 else 2
        val row = findAvailableRow(_board.value, col)

        if (row != -1) {
            _board.value = _board.value.copyWithMove(row, col, currentPlayerValue)
            when {
                // <--- CAMBIO AQUÍ: Añadir 'row' y 'col' a checkWinner
                checkWinner(_board.value, currentPlayerValue, row, col) -> _winner.value = currentPlayerValue
                isBoardFull(_board.value) -> _winner.value = 3
                else -> _playerTurn.value = !_playerTurn.value
            }
        }
    }

    fun assignWordAndStartTimer(gameId: String) {
        fetchRandomWordFromFirebase { english, spanish ->
            val gameRef = database.child("games").child(gameId)
            gameRef.get().addOnSuccessListener { snapshot ->
                val game = snapshot.getValue(Game::class.java)
                if (game != null) {
                    val updatedGame = game.copy(
                        currentWordEnglish = english,
                        correctTranslationSpanish = spanish,
                        questionAttempted = false,
                        lastGuessedCorrectly = false
                    )
                    gameRef.setValue(updatedGame)
                    startQuestionTimer(gameId)
                }
            }
        }
    }

    fun submitTranslationAnswer(gameId: String, answer: String) {
        database.child("games").child(gameId).get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            if (game != null && !game.questionAttempted) {
                val isCorrect = answer.trim().equals(game.correctTranslationSpanish.trim(), ignoreCase = true)
                val updatedGame = game.copy(
                    lastGuessedCorrectly = isCorrect,
                    questionAttempted = true
                )
                database.child("games").child(gameId).setValue(updatedGame)
                questionTimerJob?.cancel()
                if (!isCorrect) {
                    // Si es incorrecto, pasa el turno inmediatamente
                    val nextPlayerId = if (game.currentTurnPlayerId == game.player1Id) game.player2Id else game.player1Id
                    val gameAfterTurn = updatedGame.copy(
                        currentTurnPlayerId = nextPlayerId ?: "",
                        questionAttempted = false,
                        lastGuessedCorrectly = false
                    )
                    database.child("games").child(gameId).setValue(gameAfterTurn)
                }
            }
        }
    }

    private var questionTimerJob: Job? = null

    fun startQuestionTimer(gameId: String) {
        questionTimerJob?.cancel()
        questionTimerJob = viewModelScope.launch {
            kotlinx.coroutines.delay(15_000)
            database.child("games").child(gameId).get().addOnSuccessListener { snapshot ->
                val game = snapshot.getValue(Game::class.java)
                if (game != null && !game.lastGuessedCorrectly && !game.questionAttempted) {
                    // Cambia el turno al otro jugador
                    val nextPlayerId = if (game.currentTurnPlayerId == game.player1Id) game.player2Id else game.player1Id
                    val updatedGame = game.copy(
                        currentTurnPlayerId = nextPlayerId ?: "",
                        questionAttempted = false,
                        lastGuessedCorrectly = false
                    )
                    database.child("games").child(gameId).setValue(updatedGame)
                }
            }
        }
    }

    private fun fetchRandomWordFromFirebase(onResult: (english: String, spanish: String) -> Unit) {
        database.child("words").get().addOnSuccessListener { snapshot ->
            val wordsList = snapshot.children.mapNotNull { it.getValue(Word::class.java) }
            if (wordsList.isNotEmpty()) {
                val word = wordsList.random()
                onResult(word.english, word.spanish)
            }
        }
    }

    data class Word(val english: String = "", val spanish: String = "")    

    private fun handleAIMove(col: Int) {
        if (_playerTurn.value) {
            val row = findAvailableRow(_board.value, col)
            if (row != -1) {
                val currentPlayerValue = 1
                _board.value = _board.value.copyWithMove(row, col, currentPlayerValue)
                when {
                    // <--- CAMBIO AQUÍ: Añadir 'row' y 'col' a checkWinner
                    checkWinner(_board.value, currentPlayerValue, row, col) -> _winner.value = currentPlayerValue
                    isBoardFull(_board.value) -> _winner.value = 3
                    else -> {
                        _playerTurn.value = false
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(500)
                            makeAIMove()
                        }
                    }
                }
            }
        }
    }

    private fun makeAIMove() {
        val rows = _board.value.size
        val cols = _board.value[0].size
        val aiPlayerValue = 2
        var chosenRow = -1
        var chosenCol = -1

        // 1. Ganar si puede
        for (c in 0 until cols) {
            val r = findAvailableRow(_board.value, c)
            if (r != -1) {
                val tempBoard = _board.value.copyWithMove(r, c, aiPlayerValue)
                // <--- CAMBIO AQUÍ: Añadir 'r' y 'c' a checkWinner
                if (checkWinner(tempBoard, aiPlayerValue, r, c)) {
                    chosenRow = r
                    chosenCol = c
                    _board.value = tempBoard
                    _winner.value = aiPlayerValue
                    _playerTurn.value = true
                    return
                }
            }
        }

        // 2. Bloquear al jugador si va a ganar
        for (c in 0 until cols) {
            val r = findAvailableRow(_board.value, c)
            if (r != -1) {
                val tempBoard = _board.value.copyWithMove(r, c, 1)
                // <--- CAMBIO AQUÍ: Añadir 'r' y 'c' a checkWinner
                if (checkWinner(tempBoard, 1, r, c)) {
                    chosenRow = r
                    chosenCol = c
                    _board.value = _board.value.copyWithMove(r, c, aiPlayerValue)
                    // <--- CAMBIO AQUÍ: Añadir 'r' y 'c' a checkWinner
                    if (checkWinner(_board.value, aiPlayerValue, r, c)) {
                        _winner.value = aiPlayerValue
                    } else if (isBoardFull(_board.value)) {
                        _winner.value = 3
                    }
                    _playerTurn.value = true
                    return
                }
            }
        }

        // 3. Jugar en el centro (preferencia)
        val centerCol = cols / 2
        val centerRow = findAvailableRow(_board.value, centerCol)
        if (centerRow != -1) {
            chosenRow = centerRow
            chosenCol = centerCol
            _board.value = _board.value.copyWithMove(centerRow, centerCol, aiPlayerValue)
            // <--- CAMBIO AQUÍ: Añadir 'centerRow' y 'centerCol' a checkWinner
            if (checkWinner(_board.value, aiPlayerValue, centerRow, centerCol)) {
                _winner.value = aiPlayerValue
            } else if (isBoardFull(_board.value)) {
                _winner.value = 3
            }
            _playerTurn.value = true
            return
        }

        // 4. Jugar aleatoriamente en una columna válida
        val availableCols = (0 until cols).filter { c -> findAvailableRow(_board.value, c) != -1 }
        if (availableCols.isNotEmpty()) {
            val randomCol = availableCols.random()
            val r = findAvailableRow(_board.value, randomCol)
            chosenRow = r
            chosenCol = randomCol
            _board.value = _board.value.copyWithMove(r, randomCol, aiPlayerValue)
            // <--- CAMBIO AQUÍ: Añadir 'r' y 'randomCol' a checkWinner
            if (checkWinner(_board.value, aiPlayerValue, r, randomCol)) {
                _winner.value = aiPlayerValue
            } else if (isBoardFull(_board.value)) {
                _winner.value = 3
            }
            _playerTurn.value = true
        } else {
            _winner.value = 3
        }
    }


    private fun handleOnlineMove(col: Int) {
        viewModelScope.launch {
            val currentOnlineGameId = _onlineGameId.value
            val currentOnlineGameRef = onlineGameRef
            val currentOnlineGameStatus = _currentOnlineGameStatus.value
            val currentBoard = _board.value

            if (_playerTurn.value && currentOnlineGameId != null && currentOnlineGameRef != null && currentOnlineGameStatus == "playing") {
                val row = findAvailableRow(currentBoard, col)
                if (row != -1) {
                    currentOnlineGameRef.get().addOnSuccessListener { dataSnapshot ->
                        val currentOnlineGame = dataSnapshot.getValue(Game::class.java)
                        if (currentOnlineGame != null && currentOnlineGame.currentTurnPlayerId == playerLocalId) {
                            val currentPlayerValue = if (currentOnlineGame.player1Id == playerLocalId) 1 else 2
                            val nextPlayerId = if (currentOnlineGame.player1Id == playerLocalId) {
                                currentOnlineGame.player2Id
                            } else {
                                currentOnlineGame.player1Id
                            }

                            val updatedBoardList = currentOnlineGame.board.toMutableList()
                            val newRowList = updatedBoardList[row].toMutableList()
                            newRowList[col] = currentPlayerValue
                            updatedBoardList[row] = newRowList.toList()

                            val tempBoardArray = updatedBoardList.toTypedArrayOfIntArray()
                            val newWinnerValue = when {
                                // <--- CAMBIO AQUÍ: Añadir 'row' y 'col' a checkWinner
                                checkWinner(tempBoardArray, currentPlayerValue, row, col) -> currentPlayerValue
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

                            currentOnlineGameRef.setValue(updatedGame)
                                .addOnSuccessListener {
                                    println("Movimiento enviado a Firebase para la columna $col")
                                }
                                .addOnFailureListener { e ->
                                    println("Error al enviar movimiento a Firebase: ${e.message}")
                                }
                        } else {
                            println("OnlineMove: No es tu turno o la partida ha cambiado mientras intentabas mover.")
                        }
                    }.addOnFailureListener { e ->
                        println("OnlineMove: Error al obtener datos de partida para movimiento: ${e.message}")
                    }
                } else {
                    println("OnlineMove: Columna $col está llena.")
                }
            } else if (currentOnlineGameStatus != "playing") {
                println("OnlineMove: La partida online aún no ha comenzado. Esperando al otro jugador.")
            } else if (!_playerTurn.value) {
                println("OnlineMove: No es tu turno en la partida online.")
            } else {
                println("OnlineMove: Partida online no iniciada o ID nulo.")
            }
        }
    }


    // --- Funciones para Partidas Online Específicas ---
    fun createOnlineGame() {
        viewModelScope.launch {
            generateUniqueGameId(database) { newGameId ->
                if (newGameId != null) {
                    val initialBoard = List(6) { List(7) { 0 } }
                    val newGame = Game(
                        gameId = newGameId,
                        player1Id = playerLocalId,
                        player2Id = null,
                        board = initialBoard,
                        currentTurnPlayerId = playerLocalId,
                        status = "waiting",
                        winnerId = null
                    )

                    database.child("games").child(newGameId).setValue(newGame)
                        .addOnSuccessListener {
                            _onlineGameId.value = newGameId
                            _isCreatingGame.value = true
                            _board.value = initialBoard.toTypedArrayOfIntArray()
                            _winner.value = 0
                            _playerTurn.value = true
                            setupOnlineGameListener(newGameId)
                            println("Partida online creada con ID: $newGameId. Esperando jugador 2...")
                        }
                        .addOnFailureListener { e ->
                            println("Error al crear partida online: ${e.message}")
                        }
                } else {
                    println("No se pudo crear la partida: No se encontró un ID único.")
                }
            }
        }
    }

    fun joinOnlineGame(enteredGameId: String) {
        viewModelScope.launch {
            if (enteredGameId.length == 6 && enteredGameId.all { it.isDigit() }) {
                val gameRef = database.child("games").child(enteredGameId)

                gameRef.get().addOnSuccessListener { dataSnapshot ->
                    val existingGame = dataSnapshot.getValue(Game::class.java)

                    if (existingGame != null) {
                        if (existingGame.status == "waiting" && existingGame.player2Id == null) {
                            val updatedGame = existingGame.copy(
                                player2Id = playerLocalId,
                                status = "playing"
                            )

                            gameRef.setValue(updatedGame)
                                .addOnSuccessListener {
                                    _onlineGameId.value = enteredGameId
                                    _isCreatingGame.value = false
                                    setupOnlineGameListener(enteredGameId)
                                    println("Te has unido a la partida con ID: $enteredGameId")
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
    }

    // --- Funciones de Reseteo ---
    fun resetGame() {
        _board.value = Array(6) { IntArray(7) { 0 } }
        _winner.value = 0
        _playerTurn.value = true
        resetOnlineGame()
    }

    fun resetOnlineGame() {
        onlineGameRef?.removeEventListener(gameEventListener ?: return)
        onlineGameRef = null
        gameEventListener = null
        _onlineGameId.value = null
        _isCreatingGame.value = false
        _currentOnlineGameStatus.value = null
        println("Estado de partida online reseteado.")
    }
}
