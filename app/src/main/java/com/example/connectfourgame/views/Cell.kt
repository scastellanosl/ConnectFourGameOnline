package com.example.connectfourgame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Un solo disco (celda) del tablero de juego.
 * @param value El valor de la celda (0=vacío, 1=Jugador 1, 2=Jugador 2).
 * @param onClick Un lambda que se invoca cuando la celda es clickeada.
 */
@Composable
fun Cell(value: Int, onClick: () -> Unit) {
    val color = when (value) {
        1 -> Color.Red
        2 -> Color.Blue
        else -> Color.Gray // Cambiado de LightGray a Gray para mejor visibilidad en tema oscuro
    }

    Box(
        modifier = Modifier
            .size(48.dp) // Tamaño del disco
            .padding(4.dp) // Espaciado alrededor del disco
            .background(color, CircleShape) // Color y forma del disco
            .clickable { onClick() } // Hace que la celda sea clickeable
    )
}