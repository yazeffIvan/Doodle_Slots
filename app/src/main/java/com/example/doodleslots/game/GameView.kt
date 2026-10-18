package com.example.doodleslots.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.ui.graphics.toArgb
import com.example.doodleslots.R
import com.example.doodleslots.logic.GameEngine
import kotlin.concurrent.thread

class GameView(context: Context, val engine: GameEngine) : SurfaceView(context), SurfaceHolder.Callback {

    private var thread: Thread? = null
    @Volatile private var running = false

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val src = Rect()
    private val dst = Rect()
    private val dstF = RectF()

    private val bg = BitmapFactory.decodeResource(resources, R.drawable.bg)
    private val platformBmp = BitmapFactory.decodeResource(resources, R.drawable.neon_podium)
    private val playerBmp = BitmapFactory.decodeResource(resources, R.drawable.player)
    private val moonBmp = BitmapFactory.decodeResource(resources, R.drawable.moon)
    private val enemyBmp = BitmapFactory.decodeResource(resources, R.drawable.enemy)
    private val bonus1 = BitmapFactory.decodeResource(resources, R.drawable.depyvremya)
    private val bonus2 = BitmapFactory.decodeResource(resources, R.drawable.zapretdep)
    private val bonus3 = BitmapFactory.decodeResource(resources, R.drawable.comingtododep)

    var onHudUpdate: (() -> Unit)? = null

    // Игровой мир по вертикали: сколько пикселей движка "влезает" в экран
    private val WORLD_HEIGHT = 1400f
    private val WORLD_WIDTH = 960f

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(h: SurfaceHolder) {
        Log.d("GameView", "surfaceCreated")
        running = true
        thread = thread(name = "GameLoop") {
            var lastHud = 0L
            val frameNs = 16_666_667L // 60 FPS в наносекундах
            var nextFrame = System.nanoTime()

            while (running) {
                try { engine.updatePhysics() } catch (t: Throwable) { Log.e("GameView", "physics", t) }

                val c = try { h.lockCanvas() } catch (t: Throwable) { null }
                if (c != null) {
                    try {
                        synchronized(h) { drawGame(c) }
                    } catch (t: Throwable) {
                        Log.e("GameView", "draw", t)
                    } finally {
                        try { h.unlockCanvasAndPost(c) } catch (_: Throwable) {}
                    }
                }

                val now = System.currentTimeMillis()
                if (now - lastHud > 400) {
                    lastHud = now
                    post { onHudUpdate?.invoke() }
                }

                nextFrame += frameNs
                val sleepNs = nextFrame - System.nanoTime()
                if (sleepNs > 0) {
                    val ms = sleepNs / 1_000_000
                    val ns = (sleepNs % 1_000_000).toInt()
                    try { Thread.sleep(ms, ns) } catch (_: InterruptedException) {}
                } else {
                    nextFrame = System.nanoTime()
                }
            }
        }
    }

    override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, ht: Int) {
        Log.d("GameView", "surfaceChanged $w x $ht")
    }

    override fun surfaceDestroyed(h: SurfaceHolder) {
        running = false
        thread?.join(500)
        thread = null
    }

    private fun drawScaled(c: Canvas, b: Bitmap, x: Float, y: Float, w: Float, h: Float) {
        src.set(0, 0, b.width, b.height)
        dstF.set(x, y, x + w, y + h)
        c.drawBitmap(b, src, dstF, paint)
    }

    private fun drawGame(c: Canvas) {
        // Фон — на весь SurfaceView, без масштабирования мира
        bg?.let {
            src.set(0, 0, it.width, it.height)
            dst.set(0, 0, c.width, c.height)
            c.drawBitmap(it, src, dst, paint)
        } ?: c.drawColor(AndroidColor.rgb(220, 240, 255))

        // === FitViewport: сохраняем пропорции мира 960x1400, центрируем по ширине экрана ===
        val scale = c.height / WORLD_HEIGHT              // одинаковый масштаб по обеим осям
        val worldPixelWidth = WORLD_WIDTH * scale        // сколько пикселей займёт мир по ширине
        val offsetX = (c.width - worldPixelWidth) / 2f   // центрирование по горизонтали

        c.save()
        c.translate(offsetX, 0f)
        c.scale(scale, scale)

        // Луна
        moonBmp?.let { drawScaled(c, it, engine.moonX, engine.moonY, 100f, 100f) }

        // Шлейф
        val trailColor = when (engine.trailColorId) {
            "gold" -> AndroidColor.rgb(255, 215, 0)
            "purple" -> AndroidColor.rgb(156, 39, 176)
            "red" -> AndroidColor.RED
            else -> AndroidColor.GREEN
        }
        paint.color = trailColor
        for (tp in engine.trailPoints) {
            paint.alpha = (tp.alpha * 255).toInt().coerceIn(0, 255)
            c.drawCircle(tp.x, tp.y, 35f * tp.alpha, paint)
        }
        paint.alpha = 255

        // Платформы + бонусы
        for (p in engine.platforms) {
            platformBmp?.let { drawScaled(c, it, p.x, p.y, p.width, p.height) }
            val bmp = when (p.bonusType) {
                1 -> bonus1
                2 -> bonus2
                3 -> bonus3
                else -> null
            }
            bmp?.let { drawScaled(c, it, p.x + p.width / 2f - 37.5f, p.y - 80f, 75f, 75f) }
        }

        // Враги
        for (e in engine.enemies) {
            enemyBmp?.let { drawScaled(c, it, e.x, e.y, e.width, e.height) }
        }

        // Игрок
        playerBmp?.let {
            val stretch = (engine.velocityY * 0.6f).coerceIn(-12f, 12f)
            val w = 100f - stretch
            val h = 100f + stretch
            drawScaled(c, it, engine.playerX, engine.playerY, w, h)
        }

        // Частицы
        for (pt in engine.particles) {
            paint.color = pt.color.toArgb()
            paint.alpha = (pt.alpha * 255).toInt().coerceIn(0, 255)
            c.drawCircle(pt.x, pt.y, pt.size, paint)
        }
        paint.alpha = 255

        c.restore()
    }
}