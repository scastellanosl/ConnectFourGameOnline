package com.example.connectfourgame.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable

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
