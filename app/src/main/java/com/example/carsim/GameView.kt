package com.example.carsim

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View

class GameView(context: Context) : View(context) {

    private val game = Game()
    private var lastNanos = 0L
    private var running = false
    private val pointers = HashMap<Int, PointF>()

    private val choreographer = Choreographer.getInstance()

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            var dt = if (lastNanos == 0L) 0f else (frameTimeNanos - lastNanos) / 1_000_000_000f
            lastNanos = frameTimeNanos
            if (dt > 0.05f) dt = 0.05f
            game.update(dt)
            invalidate()
            choreographer.postFrameCallback(this)
        }
    }

    fun resume() {
        if (!running) {
            running = true
            lastNanos = 0L
            choreographer.postFrameCallback(frameCallback)
        }
    }

    fun pause() {
        running = false
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        game.draw(canvas, width.toFloat(), height.toFloat())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val i = event.actionIndex
                pointers[event.getPointerId(i)] = PointF(event.getX(i), event.getY(i))
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    pointers[event.getPointerId(i)] = PointF(event.getX(i), event.getY(i))
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val i = event.actionIndex
                pointers.remove(event.getPointerId(i))
            }
            MotionEvent.ACTION_CANCEL -> pointers.clear()
        }
        applyControls()
        return true
    }

    private fun applyControls() {
        val w = width.toFloat()
        val h = height.toFloat()
        var left = false
        var right = false
        var gas = false
        var brake = false
        val top = h * Ctrl.TOP
        for (p in pointers.values) {
            if (p.y < top) continue
            val fx = p.x / w
            when {
                fx < Ctrl.SL_X1 -> left = true
                fx < Ctrl.SR_X1 -> right = true
                fx >= Ctrl.GA_X0 -> gas = true
                fx >= Ctrl.BR_X0 -> brake = true
            }
        }
        game.inLeft = left
        game.inRight = right
        game.inGas = gas
        game.inBrake = brake
    }
}
