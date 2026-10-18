package com.example.doodleslots.logic

import androidx.compose.ui.graphics.Color
import com.example.doodleslots.model.EnemyModel
import com.example.doodleslots.model.Particle
import com.example.doodleslots.model.PlatformModel
import com.example.doodleslots.model.TrailPoint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.random.Random

class GameEngine {
    var playerX = 400f
    var playerY = 700f
    var velocityY = -10f
    var velocityX = 0f
    var score = 0
    var money = 100
    var speedMultiplier = 1.2f

    var inputLeft = false
    var inputRight = false
    var trailColorId = "green"

    var moonX = 200f
    var moonY = 100f

    val platforms = mutableListOf<PlatformModel>()
    val enemies = mutableListOf<EnemyModel>()
    val particles = mutableListOf<Particle>()
    val trailPoints = mutableListOf<TrailPoint>()

    private val gravity = 0.5f
    private val jumpNormal = -16f
    private val moveSpeedBase = 8f
    private val accel = 0.7f
    private val friction = 0.85f

    init {
        platforms.add(PlatformModel(350f, 1000f))
        var topY = 1000f
        while (topY > -2000f) {
            topY -= 130f
            platforms.add(PlatformModel(Random.nextFloat() * 700f, topY))
        }
    }

    fun updatePhysics() {
        val spd = speedMultiplier

        // Горизонталь с инерцией
        val targetVx = when {
            inputLeft && !inputRight -> -moveSpeedBase * spd
            inputRight && !inputLeft -> moveSpeedBase * spd
            else -> 0f
        }
        velocityX = if (targetVx != 0f) {
            velocityX + (targetVx - velocityX).coerceIn(-accel, accel)
        } else {
            if (abs(velocityX) < friction) 0f
            else velocityX - friction * sign(velocityX)
        }

        playerX += velocityX
        if (playerX < -60f) playerX = 860f
        if (playerX > 860f) playerX = -60f

        // Вертикаль
        velocityY += gravity
        playerY += velocityY

        // Шлейф
        if (velocityY < 0) {
            trailPoints.add(TrailPoint(playerX + 50f, playerY + 50f, 0.9f))
            if (trailPoints.size > 15) trailPoints.removeAt(0)
        }
        for (i in trailPoints.indices) trailPoints[i].alpha -= 0.06f
        trailPoints.removeAll { it.alpha <= 0f }

        // Частицы
        for (i in particles.indices.reversed()) {
            val pt = particles[i]
            pt.x += pt.vx
            pt.y += pt.vy
            pt.vy += 0.2f
            pt.alpha -= 0.04f
            if (pt.alpha <= 0f) particles.removeAt(i)
        }

        // Приземление на платформы
        if (velocityY > 0) {
            for (p in platforms) {
                if (playerX + 90f > p.x && playerX + 10f < p.x + p.width &&
                    playerY + 100f >= p.y && playerY + 100f <= p.y + p.height + 20f
                ) {
                    playerY = p.y - 100f
                    velocityY = jumpNormal * spd
                    money += 5
                    repeat(6) {
                        val a = Random.nextFloat() * Math.PI.toFloat()
                        val s = Random.nextFloat() * 3f + 1f
                        particles.add(Particle(playerX + 50f, playerY + 100f,
                            cos(a) * s, -sin(a) * s, 1f, Color.Cyan, 4f))
                    }
                    break
                }
            }
        }

        // Скролл вверх
        if (playerY < 400f) {
            val diff = 400f - playerY
            playerY = 400f
            score += diff.toInt()
            money += (diff / 20).toInt()

            for (i in platforms.indices) platforms[i].y += diff
            platforms.removeAll { it.y > 1400f }

            val topY = platforms.minOfOrNull { it.y } ?: 0f
            var y = topY - 130f
            while (y > -2000f) {
                platforms.add(PlatformModel(Random.nextFloat() * 700f, y))
                y -= 130f
            }
        }

        // Упал вниз
        if (playerY > 1500f) {
            playerY = 700f
            playerX = 400f
            velocityX = 0f
            velocityY = -10f
            score = 0
            money = money / 2
        }
    }
}