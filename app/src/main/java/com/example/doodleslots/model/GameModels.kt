package com.example.doodleslots.model

import androidx.compose.ui.graphics.Color

data class PlatformModel(
    var x: Float,
    var y: Float,
    val width: Float = 160f,
    val height: Float = 35f,
    var bonusType: Int? = null // 1 - суперпрыжок, 2 - зачистка врагов, 3 - кристаллы
)

data class EnemyModel(
    var x: Float,
    var y: Float,
    val width: Float = 70f,
    val height: Float = 70f,
    var speed: Float
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val color: Color,
    val size: Float
)

data class TrailPoint(
    val x: Float,
    val y: Float,
    var alpha: Float
)