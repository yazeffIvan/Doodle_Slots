package com.example.doodleslots.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.ui.graphics.toArgb
import com.example.doodleslots.R
import com.example.doodleslots.logic.GameEngine
import kotlinx.coroutines.*

class GameSurfaceView(context: Context, val gameEngine: GameEngine) : SurfaceView(context), SurfaceHolder.Callback {
    private var renderJob: Job? = null
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val dstRect = Rect()
    private val srcRect = Rect()

    private val bgBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.bg)
    private val podiumBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.neon_podium)
    private val enemyBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.enemy)
    private val playerBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.player)
    private val moonBitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.moon)

    private val bonus1: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.depyvremya)
    private val bonus2: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.zapretdep)
    private val bonus3: Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.comingtododep)

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        renderJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val canvas = holder.lockCanvas() ?: continue
                try {
                    drawGame(canvas)
                } finally {
                    holder.unlockCanvasAndPost(canvas)
                }
                delay(16L)
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderJob?.cancel()
    }

    private fun drawBitmapScaled(canvas: Canvas, bmp: Bitmap, x: Float, y: Float, w: Float, h: Float) {
        srcRect.set(0, 0, bmp.width, bmp.height)
        dstRect.set(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt())
        canvas.drawBitmap(bmp, srcRect, dstRect, paint)
    }

    private fun drawGame(canvas: Canvas) {
        if (bgBitmap != null) {
            canvas.drawBitmap(bgBitmap, null, Rect(0, 0, canvas.width, canvas.height), paint)
        } else {
            canvas.drawColor(android.graphics.Color.BLUE)
        }

        moonBitmap?.let { drawBitmapScaled(canvas, it, gameEngine.moonX, gameEngine.moonY, 100f, 100f) }

        for (p in gameEngine.platforms) {
            podiumBitmap?.let { drawBitmapScaled(canvas, it, p.x, p.y, p.width, p.height) }

            val bonusBmp = when (p.bonusType) {
                1 -> bonus1
                2 -> bonus2
                3 -> bonus3
                else -> null
            }
            bonusBmp?.let {
                drawBitmapScaled(canvas, it, p.x + (p.width / 2f) - 37.5f, p.y - 80f, 75f, 75f)
            }
        }

        for (e in gameEngine.enemies) {
            enemyBitmap?.let { drawBitmapScaled(canvas, it, e.x, e.y, e.width, e.height) }
        }

        playerBitmap?.let { drawBitmapScaled(canvas, it, gameEngine.playerX, gameEngine.playerY, 125f, 125f) }

        for (pt in gameEngine.particles) {
            paint.color = pt.color.toArgb()
            paint.alpha = (pt.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(pt.x, pt.y, pt.size, paint)
        }
    }
}