package com.example.connectfourgame.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.connectfourgame.R

/**
 * Un solo disco (celda) del tablero de juego.
 * @param value El valor de la celda (0=vacío, 1=Jugador 1, 2=Jugador 2).
 * @param onClick Un lambda que se invoca cuando la celda es clickeada.
 */
@Composable
fun Cell(value: Int, onClick: () -> Unit) {
    // Color para el "agujero" o celda vacía, ligeramente más oscuro que el tablero general
    val holeColor = Color(0xFF0D0B0B) // Un gris muy muy oscuro, casi negro

    Box(
        modifier = Modifier
            .size(48.dp) // Tamaño total de la celda/disco
            .padding(4.dp) // Espaciado interno para dar un efecto de borde alrededor del agujero/ficha
            .background(holeColor, CircleShape) // El color y forma circular del agujero
            .clickable { onClick() } // Hace que la celda sea clickeable para soltar la ficha
    ) {
        // Mostrar la imagen de la ficha solo si la celda no está vacía
        when (value) {
            1 -> Image(
                painter = painterResource(id = R.drawable.ficha_roja),
                contentDescription = "Ficha Roja",
                modifier = Modifier.matchParentSize(), // La imagen de la ficha llena completamente el espacio del agujero
                contentScale = ContentScale.FillBounds // Asegura que la imagen se estire para llenar los límites
            )
            2 -> Image(
                painter = painterResource(id = R.drawable.ficha_azul),
                contentDescription = "Ficha Azul",
                modifier = Modifier.matchParentSize(), // La imagen de la ficha llena completamente el espacio del agujero
                contentScale = ContentScale.FillBounds // Asegura que la imagen se estire para llenar los límites
            )
            // Si 'value' es 0, no se dibuja ninguna imagen, y solo se ve el 'holeColor' circular de fondo,
            // dando la ilusión del agujero vacío.
        }
    }
}