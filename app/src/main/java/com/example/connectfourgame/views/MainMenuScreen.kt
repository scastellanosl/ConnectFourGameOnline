package com.example.connectfourgame.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connectfourgame.R

@Composable
fun MainMenuScreen(
    onTwoPlayersClick: () -> Unit,
    onVsAIClick: () -> Unit,
    onOnlineClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Imagen de fondo principal del menú
        Image(
            painter = painterResource(id = R.drawable.background), // Tu imagen de fondo del menú
            contentDescription = "Fondo del Menú Principal",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .offset(y = (-100).dp) // <-- ¡AQUÍ ESTÁ EL CAMBIO! Desplaza todo el Column hacia arriba
            // Ajusta este valor (-50.dp) a más negativo para subirlo más,
            // o menos negativo para subirlo menos.
        ) {
            // Icono de la app
            Image(
                painter = painterResource(id = R.drawable.icon), // Tu icono de app
                contentDescription = "Icono de la aplicación",
                modifier = Modifier
                    .size(80.dp) // Tamaño de tu icono
                    .offset(y = (-100).dp) // Ajusta la posición vertical del icono si es necesario
            )
            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre el icono y el grupo de botones

            // --- Contenedor Principal para el Grupo de Botones Apilados ---
            val buttonGroupWidthFraction = 0.7f // El ancho de todo el bloque de botones (80% del ancho de la pantalla)
            val cornerRadius = 12.dp // Radio de las esquinas de los botones en tu imagen

            // Valores para ajustar la superposición y altura de cada "banda" de botón.
            // **AJUSTA ESTOS VALORES HASTA QUE SE VEA PERFECTO CON TU IMAGEN 'contenedor.png'**
            val buttonHeight = 60.dp // Altura visual de la banda visible de CADA botón. Puedes probar 70.dp, 80.dp, etc.
            val overlapAmount = 10.dp // Cuánto se superpone un botón al anterior. Puedes probar 20.dp, 30.dp, etc.

            // Calcula la altura total del Box que contiene los botones apilados.
            // Es la altura del primer botón + (altura - solape) de los siguientes.
            val totalStackedButtonsHeight = buttonHeight + (buttonHeight - overlapAmount) + (buttonHeight - overlapAmount)

            Box(
                modifier = Modifier
                    .fillMaxWidth(buttonGroupWidthFraction)
                    .height(totalStackedButtonsHeight) // Altura calculada para el grupo apilado
                    .clip(RoundedCornerShape(cornerRadius)) // Aplica el redondeo de las esquinas a todo el Box
            ) {
                // La imagen combinada (blue-black-gray) como fondo de este Box
                Image(
                    painter = painterResource(id = R.drawable.contenedor), // <-- TU IMAGEN COMBINADA AQUÍ
                    contentDescription = "Grupo de botones de juego apilados",
                    contentScale = ContentScale.FillBounds, // Estira la imagen para rellenar el Box contenedor
                    modifier = Modifier.fillMaxSize()
                )

                // Column para superponer las áreas clickeables y los textos
                // Usamos Arrangement.Top y Modifier.offset para controlar el apilamiento y la posición.
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top // Permite control manual con offset
                ) {
                    // Botón 1: "Play Local (2 Players)" - Banda Azul
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight) // Altura de la banda visible del botón
                            .clickable { onTwoPlayersClick() },
                        contentAlignment = Alignment.Center // Centra el texto dentro de su banda
                    ) {
                        Text(
                            "Play Local (2 Players)",
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Botón 2: "Play vs IA" - Banda Negra
                    // Aplicamos un offset negativo en Y para que se mueva hacia arriba y simule la superposición
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .offset(y = (-15).dp) // <-- AJUSTA ESTE VALOR NEGATIVO para la superposición
                            // Un valor negativo mueve el Box hacia arriba.
                            .clickable { onVsAIClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Play vs IA",
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Botón 3: "Play Online vs Friend" - Banda Gris
                    // Aplicamos un offset negativo en Y que es el doble del anterior para la segunda superposición
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .offset(y = -(overlapAmount*2)) // <-- AJUSTA ESTE VALOR NEGATIVO (aprox. el doble del anterior)
                            .clickable { onOnlineClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Play Online vs Friend",
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}