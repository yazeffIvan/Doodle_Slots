package com.example.doodleslots.ui.theme

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.doodleslots.game.GameView
import com.example.doodleslots.logic.GameEngine

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    gameEngine: GameEngine,
    money: Int,
    currentSkin: String,
    currentTrailColor: String,
    frameTrigger: Long,
    onBackToMenu: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var hudScore by remember { mutableIntStateOf(gameEngine.score) }
    var hudMoney by remember { mutableIntStateOf(gameEngine.money) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(currentTrailColor) { gameEngine.trailColorId = currentTrailColor }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { e ->
                when (e.key) {
                    Key.DirectionLeft -> {
                        when (e.type) {
                            KeyEventType.KeyDown -> gameEngine.inputLeft = true
                            KeyEventType.KeyUp -> gameEngine.inputLeft = false
                            else -> {}
                        }
                        true
                    }
                    Key.DirectionRight -> {
                        when (e.type) {
                            KeyEventType.KeyDown -> gameEngine.inputRight = true
                            KeyEventType.KeyUp -> gameEngine.inputRight = false
                            else -> {}
                        }
                        true
                    }
                    else -> false
                }
            }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                gameEngine.inputLeft = false
                gameEngine.inputRight = false
                onBackToMenu()
            }) { Text("← Меню") }

            Column(horizontalAlignment = Alignment.End) {
                Text("PnL: $hudScore м", style = MaterialTheme.typography.titleMedium)
                Text("Баланс: $hudMoney $", style = MaterialTheme.typography.bodyMedium)
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                GameView(ctx, gameEngine).also { v ->
                    v.onHudUpdate = {
                        hudScore = gameEngine.score
                        hudMoney = gameEngine.money
                    }
                }
            }
        )
    }
}