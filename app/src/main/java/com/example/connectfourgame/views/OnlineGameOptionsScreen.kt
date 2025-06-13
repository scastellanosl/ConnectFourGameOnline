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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connectfourgame.R


@Composable
fun OnlineGameOptionsScreen(
    onCreateRoomClick: () -> Unit,
    onJoinRoomClick: (gameId: String) -> Unit,
    onBackClick: () -> Unit
) {
    var gameIdInput by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_online),
            contentDescription = "Fondo del Menú Online",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(16.dp)
                .size(50.dp) // Tamaño del botón de icono
                .scale(1.5f, 1.5f)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver al menú principal",
                tint = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .offset(y = (-50).dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val buttonGroupWidthFraction = 0.7f
            val cornerRadius = 12.dp
            val buttonHeight = 60.dp
            val overlapAmount = 10.dp
            val totalStackedButtonsHeight = buttonHeight + (buttonHeight - overlapAmount) + (buttonHeight - overlapAmount)

            Box(
                modifier = Modifier
                    .fillMaxWidth(buttonGroupWidthFraction)
                    .height(totalStackedButtonsHeight)
                    .clip(RoundedCornerShape(cornerRadius))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.contenedor_online),
                    contentDescription = "Grupo de botones online apilados",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .clickable { onCreateRoomClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Crear Sala",
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .offset(y = -overlapAmount)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedTextField(
                            value = gameIdInput,
                            onValueChange = { gameIdInput = it },
                            // *** CAMBIO AQUÍ: Centrar y aplicar estilo al Label ***
                            label = {
                                Text(
                                    "ID de la Sala",
                                    color = Color.White, // Color blanco para coincidir
                                    fontSize = 20.sp, // Tamaño de fuente igual
                                    fontStyle = FontStyle.Italic, // Estilo itálico
                                    fontWeight = FontWeight.Bold, // Negrita
                                    modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
                                    textAlign = TextAlign.Center // Centra el texto
                                )
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 18.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = Color.LightGray,
                                errorTextColor = Color.Red,

                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,

                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent,

                                cursorColor = Color.White,
                                errorCursorColor = Color.Red,

                                focusedLabelColor = Color.White, // Asegúrate de que el color del label también esté en los colores del TextField
                                unfocusedLabelColor = Color.White, // Si quieres que siempre sea blanco
                                disabledLabelColor = Color.DarkGray,
                                errorLabelColor = Color.Red,

                                focusedLeadingIconColor = Color.White,
                                unfocusedLeadingIconColor = Color.LightGray,
                                disabledLeadingIconColor = Color.DarkGray,
                                errorLeadingIconColor = Color.Red,

                                focusedTrailingIconColor = Color.White,
                                unfocusedTrailingIconColor = Color.LightGray,
                                disabledTrailingIconColor = Color.DarkGray,
                                errorTrailingIconColor = Color.Red
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        keyboardController?.show()
                                    }
                                }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .offset(y = -(overlapAmount * 2))
                            .padding(horizontal = 8.dp)
                            .clickable {
                                if (gameIdInput.isNotBlank()) {
                                    onJoinRoomClick(gameIdInput)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Unirse",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}