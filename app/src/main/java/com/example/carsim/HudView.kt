package com.example.carsim

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class HudView(context: Context, private val state: GameState) : View(context) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapRoad = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapPlayer = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mapRoad.color = Color.argb(120, 140, 150, 160)
        mapRoad.strokeWidth = 3f
        mapPlayer.color = Color.rgb(235, 70, 70)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val car = state.car

        val kmh = abs(car.speed) * 4f
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 66f
        textPaint.color = Color.WHITE
        val num = kmh.toInt().toString()
        canvas.drawText(num, 44f, 104f, textPaint)
        textPaint.textSize = 26f
        textPaint.color = Color.rgb(210, 220, 230)
        canvas.drawText("km/h", 44f + textPaint.measureText(num) + 60f, 104f, textPaint)

        drawBar(canvas, 44f, 128f, 320f, 30f, car.fuel / 100f, Color.rgb(60, 200, 120), "FUEL")
        drawBar(canvas, 44f, 170f, 320f, 30f, car.damage / 100f, Color.rgb(230, 90, 80), "DAMAGE")

        coinPaint.style = Paint.Style.FILL
        coinPaint.color = Color.rgb(245, 200, 60)
        canvas.drawCircle(w - 360f, 72f, 24f, coinPaint)
        coinPaint.color = Color.rgb(200, 150, 30)
        coinPaint.style = Paint.Style.STROKE
        coinPaint.strokeWidth = 4f
        canvas.drawCircle(w - 360f, 72f, 24f, coinPaint)
        coinPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 46f
        textPaint.color = Color.WHITE
        canvas.drawText(state.coinsCollected.toString(), w - 320f, 88f, textPaint)

        drawMinimap(canvas, w)
        drawDriver(canvas, w)

        val top = h * Ctrl.TOP
        drawBtn(canvas, 0f, top, w * Ctrl.SL_X1, h, "<", state.inLeft)
        drawBtn(canvas, w * Ctrl.SL_X1, top, w * Ctrl.SR_X1, h, ">", state.inRight)
        drawBtn(canvas, w * Ctrl.BR_X0, top, w * Ctrl.GA_X0, h, "BRAKE", state.inBrake)
        drawBtn(canvas, w * Ctrl.GA_X0, top, w, h, "GAS", state.inGas)
    }

    private fun drawBar(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, frac: Float, color: Int, label: String) {
        barPaint.color = Color.argb(120, 0, 0, 0)
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 9f, 9f, barPaint)
        val fw = w * frac.coerceIn(0f, 1f)
        if (fw > 5f) {
            barFill.color = color
            canvas.drawRoundRect(RectF(x, y, x + fw, y + h), 9f, 9f, barFill)
        }
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 22f
        textPaint.color = Color.WHITE
        canvas.drawText(label, x + 12f, y + h - 8f, textPaint)
    }

    private fun drawMinimap(canvas: Canvas, w: Float) {
        val mSize = 170f
        val mx = w - mSize - 26f
        val my = 26f
        mapBg.color = Color.argb(150, 20, 24, 30)
        canvas.drawRoundRect(RectF(mx, my, mx + mSize, my + mSize), 16f, 16f, mapBg)
        val sc = mSize / GameState.WORLD
        var i = 0
        while (i < GameState.GRID) {
            val yy = my + i * GameState.ROAD_SPACING * sc
            canvas.drawLine(mx, yy, mx + mSize, yy, mapRoad)
            i++
        }
        i = 0
        while (i < GameState.GRID) {
            val xx = mx + i * GameState.ROAD_SPACING * sc
            canvas.drawLine(xx, my, xx, my + mSize, mapRoad)
            i++
        }
        canvas.drawCircle(mx + state.car.x * sc, my + state.car.z * sc, 7f, mapPlayer)
    }

    private fun drawDriver(canvas: Canvas, w: Float) {
        val cx = w / 2f
        val cy = 96f
        val r = 54f

        bodyPaint.color = Color.rgb(44, 92, 184)
        canvas.drawCircle(cx, cy + r * 1.5f, r * 1.15f, bodyPaint)

        headPaint.color = Color.rgb(246, 208, 172)
        canvas.drawCircle(cx, cy, r, headPaint)

        hairPaint.color = Color.rgb(58, 40, 30)
        canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), 180f, 180f, true, hairPaint)

        if (state.crashTimer > 0f) {
            eyePaint.color = Color.rgb(30, 30, 30)
            eyePaint.style = Paint.Style.STROKE
            eyePaint.strokeWidth = 6f
            drawX(canvas, cx - 20f, cy - 6f, 11f)
            drawX(canvas, cx + 20f, cy - 6f, 11f)
            eyePaint.style = Paint.Style.FILL
            mouthPaint.color = Color.rgb(120, 30, 30)
            canvas.drawOval(RectF(cx - 15f, cy + 16f, cx + 15f, cy + 42f), mouthPaint)
            drawBubble(canvas, cx + r + 20f, cy - 40f, "Ouch!")
        } else if (state.happyTimer > 0f) {
            eyePaint.color = Color.rgb(30, 30, 30)
            eyePaint.style = Paint.Style.STROKE
            eyePaint.strokeWidth = 6f
            canvas.drawArc(RectF(cx - 34f, cy - 26f, cx - 6f, cy + 4f), 200f, 140f, false, eyePaint)
            canvas.drawArc(RectF(cx + 6f, cy - 26f, cx + 34f, cy + 4f), 200f, 140f, false, eyePaint)
            eyePaint.style = Paint.Style.FILL
            mouthPaint.color = Color.rgb(120, 30, 30)
            canvas.drawArc(RectF(cx - 24f, cy + 4f, cx + 24f, cy + 40f), 0f, 180f, true, mouthPaint)
            drawBubble(canvas, cx + r + 20f, cy - 40f, "Nice!")
        } else {
            eyePaint.color = Color.rgb(30, 30, 30)
            eyePaint.style = Paint.Style.FILL
            canvas.drawCircle(cx - 20f, cy - 6f, 8f, eyePaint)
            canvas.drawCircle(cx + 20f, cy - 6f, 8f, eyePaint)
            eyePaint.style = Paint.Style.STROKE
            eyePaint.strokeWidth = 6f
            if (state.lowFuelWarn) {
                canvas.drawArc(RectF(cx - 20f, cy + 18f, cx + 20f, cy + 44f), 200f, 140f, false, eyePaint)
                drawBubble(canvas, cx + r + 20f, cy - 40f, "Fuel!")
            } else {
                canvas.drawArc(RectF(cx - 20f, cy + 8f, cx + 20f, cy + 34f), 20f, 140f, false, eyePaint)
            }
            eyePaint.style = Paint.Style.FILL
        }
    }

    private fun drawBubble(canvas: Canvas, x: Float, y: Float, msg: String) {
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 34f
        val tw = textPaint.measureText(msg)
        barPaint.color = Color.argb(210, 255, 255, 255)
        canvas.drawRoundRect(RectF(x, y - 40f, x + tw + 40f, y + 16f), 18f, 18f, barPaint)
        textPaint.color = Color.rgb(30, 30, 40)
        canvas.drawText(msg, x + 20f, y, textPaint)
    }

    private fun drawX(canvas: Canvas, x: Float, y: Float, s: Float) {
        canvas.drawLine(x - s, y - s, x + s, y + s, eyePaint)
        canvas.drawLine(x - s, y + s, x + s, y - s, eyePaint)
    }

    private fun drawBtn(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, label: String, pressed: Boolean) {
        val inset = 14f
        btnPaint.style = Paint.Style.FILL
        btnPaint.color = if (pressed) Color.argb(200, 90, 200, 255) else Color.argb(110, 255, 255, 255)
        canvas.drawRoundRect(RectF(l + inset, t + inset, r - inset, b - inset), 30f, 30f, btnPaint)
        btnPaint.style = Paint.Style.STROKE
        btnPaint.strokeWidth = 4f
        btnPaint.color = Color.argb(170, 255, 255, 255)
        canvas.drawRoundRect(RectF(l + inset, t + inset, r - inset, b - inset), 30f, 30f, btnPaint)
        btnPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 46f
        textPaint.color = Color.WHITE
        val cy = (t + b) / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label, (l + r) / 2f, cy, textPaint)
    }
}
