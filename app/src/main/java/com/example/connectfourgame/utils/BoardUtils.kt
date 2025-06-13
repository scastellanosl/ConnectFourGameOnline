package com.example.connectfourgame.utils

import android.util.Log
import com.google.firebase.database.DatabaseReference

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
 * Comprueba si hay cuatro piezas conectadas horizontal, vertical o diagonalmente
 * alrededor de la última pieza colocada.
 *
 * @param board El tablero de juego.
 * @param player El jugador a comprobar (1 o 2).
 * @param lastRow La fila de la última pieza colocada.
 * @param lastCol La columna de la última pieza colocada.
 * @return True si se encuentra un ganador, false en caso contrario.
 */
fun checkWinner(board: Array<IntArray>, player: Int, lastRow: Int, lastCol: Int): Boolean {
    val rows = board.size
    val cols = board[0].size

    // Comprobar horizontalmente
    // Solo necesitamos revisar la fila 'lastRow'. Iteramos 4 posiciones a la izquierda y 0 a la derecha
    // de la pieza recién colocada, ya que una línea de 4 puede empezar hasta 3 posiciones a la izquierda
    // de la pieza actual, y la pieza actual debe ser parte de ella.
    for (cOffset in -3..0) {
        val startCol = lastCol + cOffset
        if (startCol >= 0 && startCol + 3 < cols) {
            if (board[lastRow][startCol] == player &&
                board[lastRow][startCol + 1] == player &&
                board[lastRow][startCol + 2] == player &&
                board[lastRow][startCol + 3] == player
            ) {
                return true
            }
        }
    }

    // Comprobar verticalmente
    // Solo necesitamos revisar hacia abajo desde la última pieza, ya que la pieza siempre cae.
    if (lastRow + 3 < rows) {
        if (board[lastRow][lastCol] == player &&
            board[lastRow + 1][lastCol] == player &&
            board[lastRow + 2][lastCol] == player &&
            board[lastRow + 3][lastCol] == player
        ) {
            return true
        }
    }

    // Comprobar diagonales
    // Diagonal \ (arriba-izquierda a abajo-derecha)
    // Desplazamiento 'i' de -3 a 0. Esto cubre todas las posibles líneas de 4
    // que incluyen la pieza (lastRow, lastCol)
    for (i in -3..0) {
        val r = lastRow + i
        val c = lastCol + i
        if (r >= 0 && r + 3 < rows && c >= 0 && c + 3 < cols) {
            if (board[r][c] == player &&
                board[r + 1][c + 1] == player &&
                board[r + 2][c + 2] == player &&
                board[r + 3][c + 3] == player
            ) {
                return true
            }
        }
    }

    // Diagonal / (abajo-izquierda a arriba-derecha)
    // Desplazamiento 'i' de -3 a 0.
    for (i in -3..0) {
        val r = lastRow - i // Para moverse "hacia arriba" en las filas
        val c = lastCol + i // Para moverse "hacia la derecha" en las columnas
        if (r < rows && r - 3 >= 0 && c >= 0 && c + 3 < cols) {
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
    // La implementación `all` es eficiente para esta comprobación.
    return board.all { row -> row.all { it != 0 } }
}

/**
 * Crea una copia del tablero con un movimiento aplicado en [row], [col] por [player].
 * Esto es esencial para la inmutabilidad y la gestión del estado en Compose/ViewModel.
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
 * Esto es un mapeo directo y es tan eficiente como es posible para esta conversión.
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
 * La optimización aquí se centra en la lógica de reintentos y el manejo de la asincronía.
 *
 * @param database La referencia a la Realtime Database.
 * @param maxRetries El número máximo de intentos para encontrar un ID único.
 * @param callback Un lambda que recibe el ID único encontrado (o null si falla).
 */
fun generateUniqueGameId(database: DatabaseReference, maxRetries: Int = 10, callback: (String?) -> Unit) {
    if (maxRetries <= 0) {
        Log.e("GameID", "ERROR: No se pudo generar un ID de partida único después de varios intentos.")
        callback(null)
        return
    }

    val newId = (100000..999999).random().toString()

    database.child("games").child(newId).get().addOnSuccessListener { dataSnapshot ->
        if (!dataSnapshot.exists()) {
            callback(newId)
        } else {
            Log.d("GameID", "ID '$newId' ya existe, reintentando... (intentos restantes: ${maxRetries - 1})")
            generateUniqueGameId(database, maxRetries - 1, callback)
        }
    }.addOnFailureListener { e ->
        Log.e("GameID", "ERROR al verificar la unicidad del ID de partida: ${e.message}")
        callback(null)
    }
}