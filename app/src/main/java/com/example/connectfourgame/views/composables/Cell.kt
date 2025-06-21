package com.example.connectfourgame.views.composables

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.connectfourgame.R

@Composable
fun Cell(value: Int, size: Dp, onClick: () -> Unit) {
    val holeColor = Color(0xFF0D0B0B)
    Box(
        modifier = Modifier
            .size(size)
            .padding(2.dp)
            .background(holeColor, CircleShape)
            .clickable { onClick() }
    ) {
        when (value) {
            1 -> Image(
                painter = painterResource(id = R.drawable.ficha_roja),
                contentDescription = "Ficha Roja",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            2 -> Image(
                painter = painterResource(id = R.drawable.ficha_azul),
                contentDescription = "Ficha Azul",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}
