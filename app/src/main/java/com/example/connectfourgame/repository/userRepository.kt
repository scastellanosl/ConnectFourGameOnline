package com.example.connectfourgame.repository

import com.example.connectfourgame.auth_functions.logic
import com.example.connectfourgame.model.Game
import com.example.connectfourgame.model.Word
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.connectfourgame.auth_functions.logic.toTypedArrayOfIntArray
import com.example.connectfourgame.viewmodel.GameViewModel
import kotlin.jvm.java

class userRepository {

    private val database: DatabaseReference = Firebase.database.reference
    private var onlineGameRef: DatabaseReference? = null
    private var gameEventListener: ValueEventListener? = null

    fun getDataBase(): DatabaseReference {
        return database
    }

    fun getOnlineGameRef(): DatabaseReference? {
        return onlineGameRef
    }

    fun setupOnlineGameListener(
        gameId: String?,
        playerLocalId: String,
        updateVocabularyStatesFromGame: (Game) -> Unit,
        updateBoard: (Array<IntArray>) -> Unit,
        updatePlayerTurn: (Boolean) -> Unit,
        updateCurrentOnlineGameStatus: (String?) -> Unit,
        assignWordAndStartTimer: (String) -> Unit,
        updateWinner: (Int) -> Unit,
        resetOnlineGame: () -> Unit
    ) {
        onlineGameRef?.removeEventListener(gameEventListener ?: return)
        onlineGameRef = null
        gameEventListener = null
        updateCurrentOnlineGameStatus(null)

        if (gameId == null) {
            return
        }

        val gameRef = database.child("games").child(gameId)
        onlineGameRef = gameRef

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(Game::class.java)
                if (game != null) {
                    updateVocabularyStatesFromGame(game)
                    updateBoard(game.board.toTypedArrayOfIntArray())
                    updatePlayerTurn(game.currentTurnPlayerId == playerLocalId)
                    updateCurrentOnlineGameStatus(game.status)

                    if (
                        game.status == "playing" &&
                        (game.currentTurnPlayerId == playerLocalId) &&
                        !game.questionAttempted &&
                        game.currentWordEnglish.isBlank()
                    ) {
                        assignWordAndStartTimer(game.gameId)
                    }

                    updateWinner(
                        if (game.status == "finished") {
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
                    )
                } else {
                    resetOnlineGame()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        gameEventListener = listener
        gameRef.addValueEventListener(listener)
    }

    fun removeOnlineGameListener() {
        onlineGameRef?.removeEventListener(gameEventListener ?: return)
        onlineGameRef = null
        gameEventListener = null
    }

    fun assignWordAndStartTimer(
        gameId: String,
        fetchRandomWordFromFirebase: ((String, String) -> Unit) -> Unit,
        startQuestionTimer: (String) -> Unit
    ) {
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

    fun submitTranslationAnswer(
        gameId: String,
        answer: String,
        onGetGame: (Game?) -> Unit,
        onSetGame: (Game) -> Unit,
        questionTimerJobCancel: () -> Unit
    ) {
        database.child("games").child(gameId).get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            onGetGame(game)
            if (game != null && !game.questionAttempted) {
                val isCorrect = answer.trim().equals(game.correctTranslationSpanish.trim(), ignoreCase = true)
                val updatedGame = game.copy(
                    lastGuessedCorrectly = isCorrect,
                    questionAttempted = true
                )
                onSetGame(updatedGame)
                database.child("games").child(gameId).setValue(updatedGame)
                questionTimerJobCancel()
                if (!isCorrect) {
                    val nextPlayerId = if (game.currentTurnPlayerId == game.player1Id) game.player2Id else game.player1Id
                    val gameAfterTurn = updatedGame.copy(
                        currentTurnPlayerId = nextPlayerId ?: "",
                        questionAttempted = false,
                        lastGuessedCorrectly = false,
                        currentWordEnglish = "",
                        correctTranslationSpanish = ""
                    )
                    database.child("games").child(gameId).setValue(gameAfterTurn)
                }
            }
        }
    }

    fun startQuestionTimer(
        gameId: String,
        onGetGame: (Game?) -> Unit,
        onSetGame: (Game) -> Unit
    ) {
        database.child("games").child(gameId).get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            onGetGame(game)
            if (game != null && !game.lastGuessedCorrectly && !game.questionAttempted) {
                val nextPlayerId = if (game.currentTurnPlayerId == game.player1Id) game.player2Id else game.player1Id
                val updatedGame = game.copy(
                    currentTurnPlayerId = nextPlayerId ?: "",
                    questionAttempted = false,
                    lastGuessedCorrectly = false,
                    currentWordEnglish = "",
                    correctTranslationSpanish = ""
                )
                onSetGame(updatedGame)
                database.child("games").child(gameId).setValue(updatedGame)
            }
        }
    }

    fun fetchRandomWordFromFirebase(onResult: (english: String, spanish: String) -> Unit) {
        database.child("words").get().addOnSuccessListener { snapshot ->
            val wordsList = snapshot.children.mapNotNull { it.getValue(Word::class.java) }
            if (wordsList.isNotEmpty()) {
                val word = wordsList.random()
                onResult(word.english, word.spanish)
            }
        }
    }

    fun handleOnlineMove(
        onlineGameId: String?,
        onlineGameRef: DatabaseReference?,
        onlineGameStatus: String?,
        board: Array<IntArray>,
        playerTurn: Boolean,
        playerLocalId: String,
        questionAttempted: Boolean,
        lastGuessedCorrectly: Boolean,
        col: Int,
        onGetGame: (Game?) -> Unit,
        onSetGame: (Game) -> Unit,
        checkWinner: (Array<IntArray>, Int, Int, Int) -> Boolean,
        isBoardFull: (Array<IntArray>) -> Boolean
    ) {
        if (playerTurn && onlineGameId != null && onlineGameRef != null && onlineGameStatus == "playing") {
            val row = logic.findAvailableRow(board, col)
            if (row != -1) {
                onlineGameRef.get().addOnSuccessListener { dataSnapshot ->
                    val currentOnlineGame = dataSnapshot.getValue(Game::class.java)
                    onGetGame(currentOnlineGame)
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
                            },
                            currentWordEnglish = "",
                            correctTranslationSpanish = "",
                            questionAttempted = false,
                            lastGuessedCorrectly = false
                        )

                        onSetGame(updatedGame)
                        onlineGameRef.setValue(updatedGame)
                    }
                }
            }
        }
    }

    fun createOnlineGame(
        playerLocalId: String,
        generateUniqueGameId: (DatabaseReference, (String?) -> Unit) -> Unit,
        onGameCreated: (String, List<List<Int>>) -> Unit
    ) {
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
                        onGameCreated(newGameId, initialBoard)
                    }
            }
        }
    }

    fun joinOnlineGame(
        enteredGameId: String,
        playerLocalId: String,
        onGameJoined: (String) -> Unit
    ) {
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
                                onGameJoined(enteredGameId)
                            }
                    }
                }
            }
        }
    }
}