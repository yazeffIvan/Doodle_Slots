package com.example.doodleslots.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuScreen(
    money: Int,
    currentSpeed: Float,
    onPlay: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DOODLE SLOTS", fontSize = 36.sp)
        Spacer(Modifier.height(8.dp))
        Text("Баланс: $money")
        Spacer(Modifier.height(32.dp))

        Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Играть", fontSize = 20.sp)
        }

        Spacer(Modifier.height(32.dp))

        Text("Скорость: ${"%.1f".format(currentSpeed)}x")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1.0f, 1.5f, 2.0f, 3.0f).forEach { s ->
                Button(
                    onClick = { onSpeedChange(s) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSpeed == s) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("${s}x") }
            }
        }
    }
}