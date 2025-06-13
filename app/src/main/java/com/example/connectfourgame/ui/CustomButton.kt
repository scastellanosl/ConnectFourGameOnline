package com.example.connectfourgame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color // Para el color del texto

@Composable
fun CustomMenuButton(
    text: String,
    backgroundImagePainter: Painter, // La imagen de fondo del botón
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Puedes añadir más parámetros de estilo si necesitas, por ejemplo, el color del texto
    textColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Altura fija para los botones, ajusta según necesidad
            .clip(RoundedCornerShape(20.dp)) // Redondea las esquinas
            .clickable(onClick = onClick), // Hace que todo el Box sea clickeable
        contentAlignment = Alignment.Center // Centra el contenido (texto) dentro del Box
    ) {
        // La imagen de fondo
        Image(
            painter = backgroundImagePainter,
            contentDescription = null, // La descripción de contenido no es necesaria para un fondo
            contentScale = ContentScale.FillBounds, // Ajusta la imagen para llenar el Box
            modifier = Modifier.fillMaxSize()
        )

        // El texto superpuesto
        Text(
            text = text,
            fontSize = 20.sp, // Ajusta el tamaño de la fuente
            fontStyle = FontStyle.Italic, // Cursiva
            fontWeight = FontWeight.Bold, // Negrita
            color = textColor // Color del texto
        )
    }
}