package com.example.connectfourgame.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.connectfourgame.model.*
import com.example.connectfourgame.utils.*
import com.example.connectfourgame.repository.userRepository
import com.example.connectfourgame.auth_functions.logic
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _lastGuessedCorrectly = MutableStateFlow(false)
    val lastGuessedCorrectly: StateFlow<Boolean> = _lastGuessedCorrectly

    private val prefs = application.getSharedPreferences("connect4_prefs", Context.MODE_PRIVATE)
    private val _playerLocalId = loadOrCreatePlayerId()
    val playerLocalId: String get() = _playerLocalId

    private fun loadOrCreatePlayerId(): String {
        val existing = prefs.getString("player_id", null)
        return if (existing != null) {
            existing
        } else {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("player_id", newId).apply()
            newId
        }
    }

    private val _board = MutableStateFlow(Array(6) { IntArray(7) { 0 } })
    val board: StateFlow<Array<IntArray>> = _board.asStateFlow()

    private val _playerTurn = MutableStateFlow(true)
    val playerTurn: StateFlow<Boolean> = _playerTurn.asStateFlow()

    private val _winner = MutableStateFlow(0)
    val winner: StateFlow<Int> = _winner.asStateFlow()

    private val _gameMode = MutableStateFlow<GameMode?>(null)
    val gameMode: StateFlow<GameMode?> = _gameMode.asStateFlow()

    private val repo = userRepository()

    private val _onlineGameId = MutableStateFlow<String?>(null)
    val onlineGameId: StateFlow<String?> = _onlineGameId.asStateFlow()

    private val _isCreatingGame = MutableStateFlow(false)
    val isCreatingGame: StateFlow<Boolean> = _isCreatingGame.asStateFlow()

    private val _currentOnlineGameStatus = MutableStateFlow<String?>(null)
    val currentOnlineGameStatus: StateFlow<String?> = _currentOnlineGameStatus.asStateFlow()

    private val _currentWordEnglish = MutableStateFlow("")
    val currentWordEnglish: StateFlow<String> = _currentWordEnglish

    private val _questionAttempted = MutableStateFlow(false)
    val questionAttempted: StateFlow<Boolean> = _questionAttempted

    private val _secondsLeft = MutableStateFlow(15)
    val secondsLeft: StateFlow<Int> = _secondsLeft

    fun updateVocabularyStatesFromGame(game: Game) {
        _currentWordEnglish.value = game.currentWordEnglish
        _questionAttempted.value = game.questionAttempted
        _lastGuessedCorrectly.value = game.lastGuessedCorrectly
    }

    init {
        resetGame()
    }

    fun setupOnlineGameListener(gameId: String?) {
        repo.setupOnlineGameListener(
            gameId,
            playerLocalId,
            { game -> updateVocabularyStatesFromGame(game) },
            { board -> _board.value = board },
            { turn -> _playerTurn.value = turn },
            { status -> _currentOnlineGameStatus.value = status },
            { id -> assignWordAndStartTimer(id) },
            { winner -> _winner.value = winner },
            { resetOnlineGame() }
        )
    }

    override fun onCleared() {
        super.onCleared()
        repo.removeOnlineGameListener()
    }

    fun setGameMode(mode: GameMode?) {
        _gameMode.value = mode
        resetGame()
    }

    fun dropDisc(col: Int) {
        val currentWinner = _winner.value
        val currentMode = _gameMode.value

        if (currentWinner != 0) return

        when (currentMode) {
            GameMode.TWO_PLAYERS -> handleLocalMove(col)
            GameMode.VS_AI -> handleAIMove(col)
            GameMode.ONLINE -> {
                // Si quieres que solo se pueda poner ficha tras responder correctamente, deja la condición.
                // Si quieres permitir siempre, elimina la condición.
                if (_questionAttempted.value && _lastGuessedCorrectly.value) {
                    handleOnlineMove(col)
                }
            }
            null -> { }
        }
    }

    private fun handleLocalMove(col: Int) {
        val currentPlayerValue = if (_playerTurn.value) 1 else 2
        val row = logic.findAvailableRow(_board.value, col)

        if (row != -1) {
            _board.value = _board.value.copyWithMove(row, col, currentPlayerValue)
            when {
                logic.checkWinner(_board.value, currentPlayerValue, row, col) -> _winner.value = currentPlayerValue
                logic.isBoardFull(_board.value) -> _winner.value = 3
                else -> _playerTurn.value = !_playerTurn.value
            }
        }
    }

    fun assignWordAndStartTimer(gameId: String) {
        repo.assignWordAndStartTimer(
            gameId,
            { cb -> repo.fetchRandomWordFromFirebase(cb) },
            { id -> startQuestionTimer(id) }
        )
    }

    fun submitTranslationAnswer(gameId: String, answer: String) {
        repo.submitTranslationAnswer(
            gameId,
            answer,
            { game -> },
            { updatedGame -> },
            { questionTimerJob?.cancel() }
        )
    }

    private var questionTimerJob: Job? = null

    fun startQuestionTimer(gameId: String) {
        questionTimerJob?.cancel()
        questionTimerJob = viewModelScope.launch {
            _secondsLeft.value = 15
            for (i in 14 downTo 0) {
                kotlinx.coroutines.delay(1000)
                _secondsLeft.value = i
            }
            repo.startQuestionTimer(
                gameId,
                { game -> },
                { updatedGame -> }
            )
        }
    }

    data class Word(val english: String = "", val spanish: String = "")

    private fun handleAIMove(col: Int) {
        if (_playerTurn.value) {
            val row = logic.findAvailableRow(_board.value, col)
            if (row != -1) {
                val currentPlayerValue = 1
                _board.value = _board.value.copyWithMove(row, col, currentPlayerValue)
                when {
                    logic.checkWinner(_board.value, currentPlayerValue, row, col) -> _winner.value = currentPlayerValue
                    logic.isBoardFull(_board.value) -> _winner.value = 3
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

        for (c in 0 until cols) {
            val r = logic.findAvailableRow(_board.value, c)
            if (r != -1) {
                val tempBoard = _board.value.copyWithMove(r, c, aiPlayerValue)
                if (logic.checkWinner(tempBoard, aiPlayerValue, r, c)) {
                    _board.value = tempBoard
                    _winner.value = aiPlayerValue
                    _playerTurn.value = true
                    return
                }
            }
        }

        for (c in 0 until cols) {
            val r = logic.findAvailableRow(_board.value, c)
            if (r != -1) {
                val tempBoard = _board.value.copyWithMove(r, c, 1)
                if (logic.checkWinner(tempBoard, 1, r, c)) {
                    _board.value = _board.value.copyWithMove(r, c, aiPlayerValue)
                    if (logic.checkWinner(_board.value, aiPlayerValue, r, c)) {
                        _winner.value = aiPlayerValue
                    } else if (logic.isBoardFull(_board.value)) {
                        _winner.value = 3
                    }
                    _playerTurn.value = true
                    return
                }
            }
        }

        val centerCol = cols / 2
        val centerRow = logic.findAvailableRow(_board.value, centerCol)
        if (centerRow != -1) {
            _board.value = _board.value.copyWithMove(centerRow, centerCol, aiPlayerValue)
            if (logic.checkWinner(_board.value, aiPlayerValue, centerRow, centerCol)) {
                _winner.value = aiPlayerValue
            } else if (logic.isBoardFull(_board.value)) {
                _winner.value = 3
            }
            _playerTurn.value = true
            return
        }

        val availableCols = (0 until cols).filter { c -> logic.findAvailableRow(_board.value, c) != -1 }
        if (availableCols.isNotEmpty()) {
            val randomCol = availableCols.random()
            val r = logic.findAvailableRow(_board.value, randomCol)
            _board.value = _board.value.copyWithMove(r, randomCol, aiPlayerValue)
            if (logic.checkWinner(_board.value, aiPlayerValue, r, randomCol)) {
                _winner.value = aiPlayerValue
            } else if (logic.isBoardFull(_board.value)) {
                _winner.value = 3
            }
            _playerTurn.value = true
        } else {
            _winner.value = 3
        }
    }

    private fun handleOnlineMove(col: Int) {
        viewModelScope.launch {
            repo.handleOnlineMove(
                _onlineGameId.value,
                repo.getOnlineGameRef(), // Siempre usa el valor actualizado
                _currentOnlineGameStatus.value,
                _board.value,
                _playerTurn.value,
                playerLocalId,
                _questionAttempted.value,
                _lastGuessedCorrectly.value,
                col,
                { game -> },
                { updatedGame -> },
                { board, player, row, col -> logic.checkWinner(board, player, row, col) },
                { board -> logic.isBoardFull(board) }
            )
        }
    }

    fun createOnlineGame() {
        viewModelScope.launch {
            repo.createOnlineGame(
                playerLocalId,
                { db, cb -> generateUniqueGameId(db, 5, cb) },
                { newGameId, initialBoard ->
                    _onlineGameId.value = newGameId
                    _isCreatingGame.value = true
                    _board.value = initialBoard.toTypedArrayOfIntArray()
                    _winner.value = 0
                    _playerTurn.value = true
                    setupOnlineGameListener(newGameId)
                }
            )
        }
    }

    fun joinOnlineGame(enteredGameId: String) {
        viewModelScope.launch {
            repo.joinOnlineGame(
                enteredGameId,
                playerLocalId,
                { id ->
                    _onlineGameId.value = id
                    _isCreatingGame.value = false
                    setupOnlineGameListener(id)
                }
            )
        }
    }

    fun resetGame() {
        _board.value = Array(6) { IntArray(7) { 0 } }
        _winner.value = 0
        _playerTurn.value = true
        resetOnlineGame()
    }

    fun resetOnlineGame() {
        repo.removeOnlineGameListener()
        _onlineGameId.value = null
        _isCreatingGame.value = false
        _currentOnlineGameStatus.value = null
    }
}