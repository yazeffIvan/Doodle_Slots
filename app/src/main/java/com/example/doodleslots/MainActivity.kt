package com.example.doodleslots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.doodleslots.data.GameRepository
import com.example.doodleslots.logic.GameEngine
import com.example.doodleslots.ui.theme.GameScreen
import com.example.doodleslots.ui.theme.MenuScreen
import com.example.doodleslots.presentation.shop.ShopScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { Root() }
            }
        }
    }
}

@Composable
fun Root() {
    val ctx = LocalContext.current
    val repo = remember { GameRepository(ctx) }

    var screen by remember { mutableStateOf("menu") }
    var speed by remember { mutableStateOf(repo.getSpeedMultiplier()) }
    var engine by remember { mutableStateOf<GameEngine?>(null) }
    var money by remember { mutableStateOf(repo.getMoney()) }

    when (screen) {
        "menu" -> MenuScreen(
            money = money,
            currentSpeed = speed,
            onPlay = {
                val e = GameEngine().apply {
                    speedMultiplier = speed
                    this.money = money
                }
                engine = e
                screen = "game"
            },
            onSpeedChange = {
                speed = it
                repo.saveSpeedMultiplier(it)
            }
        )
        "game" -> {
            val e = engine ?: return
            GameScreen(
                gameEngine = e,
                money = money,
                currentSkin = "default",
                currentTrailColor = "green",
                frameTrigger = 0L,
                onBackToMenu = {
                    money = e.money
                    repo.saveMoney(money)
                    screen = "menu"
                }
            )
        }
    }
}