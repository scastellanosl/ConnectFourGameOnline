package com.example.connectfourgame.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Board(board: Array<IntArray>, onColumnClick: (Int) -> Unit) {
    // Color de fondo para el cuerpo principal del tablero (el rectángulo oscuro)
    val boardBackgroundColor = Color(0xFF1D1D1D) // Un gris muy oscuro

    Column(
        modifier = Modifier
            .fillMaxWidth() // El tablero ocupará todo el ancho disponible
            .wrapContentHeight() // La altura se ajustará al contenido (filas de celdas)
            .clip(RoundedCornerShape(16.dp)) // Aplica esquinas redondeadas al tablero
            .background(boardBackgroundColor) // Aplica el color de fondo oscuro al tablero
            .padding(horizontal = 8.dp, vertical = 8.dp) // Espaciado interno entre el borde del tablero y las celdas
    ) {
        for (row in board) {
            Row {
                for ((colIndex, cell) in row.withIndex()) {
                    // Cada 'Cell' ahora representará un agujero y mostrará la ficha si hay una
                    Cell(cell) { onColumnClick(colIndex) }
                }
            }
        }
    }
}