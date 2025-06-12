package com.example.connectfourgame.utils

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