package com.example.carsim

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class GameGLSurfaceView(context: Context, private val state: GameState) : GLSurfaceView(context) {

    private val pointers = HashMap<Int, PointF>()

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 0, 16, 0)
        renderMode = RENDERMODE_CONTINUOUSLY
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
        state.inLeft = left
        state.inRight = right
        state.inGas = gas
        state.inBrake = brake
    }
}
