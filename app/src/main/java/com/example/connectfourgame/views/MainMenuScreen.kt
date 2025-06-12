package com.example.connectfourgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onTwoPlayersClick: () -> Unit,
    onVsAIClick: () -> Unit,
    onOnlineClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onTwoPlayersClick) {
            Text("Jugar Local (2 Jugadores)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVsAIClick) {
            Text("Jugar contra la IA")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOnlineClick) {
            Text("Jugar Online")
        }
    }
}