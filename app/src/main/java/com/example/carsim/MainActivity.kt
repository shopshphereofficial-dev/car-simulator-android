package com.example.carsim

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout

class MainActivity : Activity() {

    private lateinit var glView: GameGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val state = GameState()
        val hud = HudView(this, state)
        glView = GameGLSurfaceView(this, state)
        glView.setRenderer(GameRenderer(state, hud))

        val root = FrameLayout(this)
        root.addView(glView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        root.addView(hud, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }
}
