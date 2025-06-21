package com.example.connectfourgame.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun Board(board: Array<IntArray>, onColumnClick: (Int) -> Unit) {
    val boardBackgroundColor = Color(0xFF1D1D1D)
    val columns = board[0].size
    val rows = board.size

    // Calcula el ancho de pantalla disponible
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val horizontalPadding = 16.dp // 8.dp a cada lado
    val cellSpacing = 8.dp // Espaciado entre celdas

    // Calcula el tamaño de cada celda para que el tablero ocupe el ancho máximo posible
    val totalSpacing = cellSpacing * (columns + 1)
    val cellSize = ((screenWidthDp - horizontalPadding - totalSpacing) / columns).coerceAtLeast(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(boardBackgroundColor)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        for (row in board) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = cellSpacing / 4),
                horizontalArrangement = Arrangement.Center
            ) {
                for ((colIndex, cell) in row.withIndex()) {
                    Cell(
                        value = cell,
                        size = cellSize
                    ) { onColumnClick(colIndex) }
                    Spacer(modifier = Modifier.width(cellSpacing / 2))
                }
            }
        }
    }
}
