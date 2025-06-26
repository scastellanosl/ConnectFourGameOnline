package com.example.connectfourgame

import com.example.connectfourgame.utils.checkWinner
import com.example.connectfourgame.utils.isBoardFull
import com.example.connectfourgame.utils.findAvailableRow
import com.example.connectfourgame.utils.copyWithMove

import org.junit.Assert.*
import org.junit.Test

class GameLogicTest {

    // Helper to create a new board for testing
    private fun createEmptyBoard(rows: Int = 6, cols: Int = 7): Array<IntArray> {
        return Array(rows) { IntArray(cols) { 0 } }
    }

    @Test
    fun testHorizontalWin() {
        val board = createEmptyBoard()
        // Player 1 places 4 pieces horizontally in the first row
        for (i in 0..3) board[0][i] = 1
        // The last piece was placed at (0, 3)
        assertTrue(checkWinner(board, 1, 0, 3))
    }

    @Test
    fun testVerticalWin() {
        val board = createEmptyBoard()
        // Corregido: Coloca las piezas en la parte inferior de la columna
        // Asumiendo un tablero de 6 filas (índices 0-5), la fila más baja es 5.
        // Las piezas se colocarán en las filas 5, 4, 3, 2.
        board[5][0] = 2
        board[4][0] = 2
        board[3][0] = 2
        board[2][0] = 2 // Esta es la última pieza colocada que completa la línea

        // La última pieza se colocó en (2, 0) (fila 2, columna 0)
        assertTrue(checkWinner(board, 2, 2, 0))
    }

    @Test
    fun testDiagonalWinBottomLeftToTopRight() {
        val board = createEmptyBoard()
        board[3][0] = 1 // (3,0)
        board[2][1] = 1 // (2,1)
        board[1][2] = 1 // (1,2)
        board[0][3] = 1 // (0,3) - last piece
        assertTrue(checkWinner(board, 1, 0, 3))
    }

    @Test
    fun testDiagonalWinTopLeftToBottomRight() {
        val board = createEmptyBoard()
        board[0][0] = 2 // (0,0)
        board[1][1] = 2 // (1,1)
        board[2][2] = 2 // (2,2)
        board[3][3] = 2 // (3,3) - last piece
        assertTrue(checkWinner(board, 2, 3, 3))
    }

    @Test
    fun testNoWin() {
        val board = createEmptyBoard()
        board[0][0] = 1
        board[0][1] = 1
        board[0][2] = 1
        board[0][3] = 2 // Blocks the win
        // Even if player 1 places a piece next, there's no win for player 1 at (0,3)
        assertFalse(checkWinner(board, 1, 0, 3))
    }

    @Test
    fun testNoWinPartialLine() {
        val board = createEmptyBoard()
        board[0][0] = 1
        board[0][1] = 1
        board[0][2] = 1
        // No fourth piece
        assertFalse(checkWinner(board, 1, 0, 2))
    }

    @Test
    fun testBoardFull() {
        val board = Array(6) { IntArray(7) { 1 } } // All cells filled with player 1's pieces
        assertTrue(isBoardFull(board))
    }

    @Test
    fun testBoardNotFull() {
        val board = createEmptyBoard()
        board[0][0] = 1 // One piece placed
        assertFalse(isBoardFull(board))
    }

    @Test
    fun testEmptyBoardIsNotFull() {
        val board = createEmptyBoard() // All cells are 0
        assertFalse(isBoardFull(board))
    }

    @Test
    fun testFindAvailableRowInPartiallyFilledColumn() {
        val board = createEmptyBoard()
        board[5][0] = 1 // Bottom piece
        board[4][0] = 2 // Next piece
        // The next available row in column 0 should be 3 (index 3)
        assertEquals(3, findAvailableRow(board, 0))
    }

    @Test
    fun testFindAvailableRowInEmptyColumn() {
        val board = createEmptyBoard()
        // For an empty column, the lowest row (index 5) should be available
        assertEquals(5, findAvailableRow(board, 0))
    }

    @Test
    fun testFindAvailableRowInFullColumn() {
        val board = createEmptyBoard()
        // Fill the entire first column
        for (r in 0 until 6) {
            board[r][0] = 1
        }
        // No available row, should return -1
        assertEquals(-1, findAvailableRow(board, 0))
    }

    @Test
    fun testCopyWithMoveAddsPieceCorrectly() {
        val originalBoard = createEmptyBoard(3, 3) // Create a smaller board for simplicity
        val newBoard = originalBoard.copyWithMove(2, 1, 1) // Place player 1 at (2,1)

        // Verify the new piece is in place
        assertEquals(1, newBoard[2][1])
    }

    @Test
    fun testCopyWithMoveDoesNotModifyOriginalBoard() {
        val originalBoard = createEmptyBoard(3, 3)
        val newBoard = originalBoard.copyWithMove(2, 1, 1)

        // Ensure the original board at the moved position is still 0
        assertEquals(0, originalBoard[2][1])
        // Also, confirm that the newBoard is a different instance
        assertFalse(originalBoard === newBoard)
    }

    @Test
    fun testCopyWithMoveCopiesOtherCellsCorrectly() {
        val originalBoard = arrayOf(
            intArrayOf(0, 0, 0),
            intArrayOf(0, 1, 0),
            intArrayOf(0, 0, 0)
        )
        val newBoard = originalBoard.copyWithMove(0, 0, 2) // Place player 2 at (0,0)

        // Verify the new piece
        assertEquals(2, newBoard[0][0])
        // Verify other existing pieces are still there
        assertEquals(1, newBoard[1][1])
        // Verify other empty cells remain empty
        assertEquals(0, newBoard[0][1])
    }
}