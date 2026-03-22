package com.ablauncher.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.View
import com.ablauncher.data.datastore.PreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.sin

@AndroidEntryPoint
class ABDreamService : DreamService() {

    @Inject
    lateinit var prefsDataStore: PreferencesDataStore

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true

        val style = runBlocking { prefsDataStore.screensaverStyle.first() }
        val dreamView: View = when (style) {
            "GRADIENT" -> GradientDreamView(this)
            "COLORS"   -> ColorsDreamView(this)
            else       -> ClockDreamView(this)
        }
        setContentView(dreamView)
    }
}

// ── Clock screensaver ─────────────────────────────────────────────────────────
// Floating time + date that drifts slowly to prevent screen burn-in.

private class ClockDreamView(context: Context) : View(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d")

    private var timeText = ""
    private var dateText = ""
    private var driftX = 0f
    private var driftY = 0f
    private var tick = 0

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 160f
    }
    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 255, 255)
        textAlign = Paint.Align.CENTER
        textSize = 52f
    }

    private val ticker = object : Runnable {
        override fun run() {
            timeText = LocalTime.now().format(timeFmt)
            dateText = LocalDate.now().format(dateFmt)
            // Lissajous-style drift to avoid burn-in on OLED screens
            driftX = sin(tick * 0.018f) * (width * 0.12f)
            driftY = sin(tick * 0.011f) * (height * 0.08f)
            tick++
            invalidate()
            handler.postDelayed(this, 1_000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(ticker)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(ticker)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        val cx = width / 2f + driftX
        val cy = height / 2f + driftY
        canvas.drawText(timeText, cx, cy, timePaint)
        canvas.drawText(dateText, cx, cy + 80f, datePaint)
    }
}

// ── Gradient screensaver ──────────────────────────────────────────────────────
// Slowly cycles through deep gradient pairs.

private class GradientDreamView(context: Context) : View(context) {

    private val handler = Handler(Looper.getMainLooper())

    private val palette = intArrayOf(
        Color.parseColor("#0D0D2B"),
        Color.parseColor("#1A3A4A"),
        Color.parseColor("#0D2B1A"),
        Color.parseColor("#1A0533"),
        Color.parseColor("#001A2E"),
        Color.parseColor("#1A0800"),
        Color.parseColor("#000D1A"),
        Color.parseColor("#0A1A0A"),
    )

    private val paint = Paint()
    private var phase = 0f   // 0..1 cycling through palette

    private val updater = object : Runnable {
        override fun run() {
            phase = (phase + 0.004f) % 1f
            invalidate()
            handler.postDelayed(this, 50)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(updater)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updater)
    }

    override fun onDraw(canvas: Canvas) {
        val n = palette.size
        val f = phase * n
        val idx0 = f.toInt() % n
        val idx1 = (idx0 + 1) % n
        paint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            palette[idx0], palette[idx1],
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}

// ── Colors screensaver ────────────────────────────────────────────────────────
// Slowly cycles the full HSV hue wheel as a solid fill.

private class ColorsDreamView(context: Context) : View(context) {

    private val handler = Handler(Looper.getMainLooper())
    private var hue = 0f
    private val paint = Paint()

    private val updater = object : Runnable {
        override fun run() {
            hue = (hue + 0.25f) % 360f
            invalidate()
            handler.postDelayed(this, 50)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(updater)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(updater)
    }

    override fun onDraw(canvas: Canvas) {
        paint.color = Color.HSVToColor(floatArrayOf(hue, 0.65f, 0.25f))
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
}
