package com.example.connectfourgame.model

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