package com.example.connectfourgame.auth_functions

object logic {

    fun findAvailableRow(board: Array<IntArray>, col: Int): Int {
        for (row in board.size - 1 downTo 0) {
            if (board[row][col] == 0) return row
        }
        return -1
    }

    fun checkWinner(board: Array<IntArray>, player: Int, row: Int, col: Int): Boolean {
        val directions = listOf(
            Pair(0, 1), Pair(1, 0), Pair(1, 1), Pair(1, -1)
        )
        for ((dr, dc) in directions) {
            var count = 1
            var r = row + dr
            var c = col + dc
            while (r in board.indices && c in board[0].indices && board[r][c] == player) {
                count++
                r += dr
                c += dc
            }
            r = row - dr
            c = col - dc
            while (r in board.indices && c in board[0].indices && board[r][c] == player) {
                count++
                r -= dr
                c -= dc
            }
            if (count >= 4) return true
        }
        return false
    }

    fun isBoardFull(board: Array<IntArray>): Boolean {
        for (row in board) {
            for (cell in row) {
                if (cell == 0) return false
            }
        }
        return true
    }

    fun Array<IntArray>.copyWithMove(row: Int, col: Int, player: Int): Array<IntArray> {
        val newBoard = Array(size) { get(it).clone() }
        newBoard[row][col] = player
        return newBoard
    }

    fun List<List<Int>>.toTypedArrayOfIntArray(): Array<IntArray> {
        return map { it.toIntArray() }.toTypedArray()
    }
}