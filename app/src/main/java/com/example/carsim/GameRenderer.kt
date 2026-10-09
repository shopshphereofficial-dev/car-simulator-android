package com.example.carsim

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer(private val state: GameState, private val hud: HudView) : GLSurfaceView.Renderer {

    private var program = 0
    private var aPos = 0
    private var aNormal = 0
    private var uMVP = 0
    private var uModel = 0
    private var uColor = 0
    private var uLightDir = 0
    private lateinit var posBuf: FloatBuffer
    private lateinit var norBuf: FloatBuffer
    private var lastNanos = 0L

    private val proj = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)
    private val tmp = FloatArray(16)
    private val parent = FloatArray(16)
    private val ident = FloatArray(16).also { Matrix.setIdentityM(it, 0) }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.53f, 0.72f, 0.92f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        program = buildProgram(VERT_SRC, FRAG_SRC)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aNormal = GLES20.glGetAttribLocation(program, "aNormal")
        uMVP = GLES20.glGetUniformLocation(program, "uMVP")
        uModel = GLES20.glGetUniformLocation(program, "uModel")
        uColor = GLES20.glGetUniformLocation(program, "uColor")
        uLightDir = GLES20.glGetUniformLocation(program, "uLightDir")

        val cube = makeCube()
        posBuf = cube.first
        norBuf = cube.second

        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glEnableVertexAttribArray(aNormal)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(proj, 0, 55f, aspect, 0.5f, 900f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        var dt = if (lastNanos == 0L) 0f else (now - lastNanos) / 1_000_000_000f
        lastNanos = now
        if (dt > 0.05f) dt = 0.05f

        state.update(dt)
        hud.postInvalidate()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)

        val a = state.car.angle
        val fx = cos(a)
        val fz = sin(a)
        val ex = state.car.x - fx * 12f
        val ez = state.car.z - fz * 12f
        Matrix.setLookAtM(
            view, 0,
            ex, 7f, ez,
            state.car.x + fx * 4f, 1.4f, state.car.z + fz * 4f,
            0f, 1f, 0f
        )

        GLES20.glUniform3f(uLightDir, 0.45f, 0.85f, 0.35f)

        drawWorld()
    }

    private fun drawWorld() {
        val W = GameState.WORLD
        val S = GameState.ROAD_SPACING
        val G = W + 260f

        drawBox(null, W / 2f, -1f, W / 2f, G, 2f, G, 0.30f, 0.52f, 0.28f, 0f)

        var i = 0
        while (i < GameState.GRID) {
            val c = i * S
            drawBox(null, W / 2f, 0.05f, c, W + 60f, 0.2f, GameState.ROAD_HALF * 2f, 0.24f, 0.25f, 0.27f, 0f)
            drawBox(null, c, 0.05f, W / 2f, GameState.ROAD_HALF * 2f, 0.2f, W + 60f, 0.24f, 0.25f, 0.27f, 0f)
            drawBox(null, W / 2f, 0.16f, c, W, 0.05f, 0.4f, 0.92f, 0.82f, 0.22f, 0f)
            drawBox(null, c, 0.16f, W / 2f, 0.4f, 0.05f, W, 0.92f, 0.82f, 0.22f, 0f)
            i++
        }

        for (s in state.stations) {
            drawBox(null, s.x, 0.15f, s.z, 9f, 0.3f, 9f, 0.16f, 0.62f, 0.36f, 0f)
            drawBox(null, s.x - 2.5f, 1.6f, s.z, 0.8f, 3f, 0.8f, 0.92f, 0.92f, 0.92f, 0f)
        }

        for (b in state.buildings) {
            drawBox(null, b.x, b.h / 2f, b.z, b.w, b.h, b.d, b.cr, b.cg, b.cb, 0f)
            drawBox(null, b.x, b.h + 0.4f, b.z, b.w * 1.04f, 0.8f, b.d * 1.04f, b.cr * 0.72f, b.cg * 0.72f, b.cb * 0.72f, 0f)
        }

        for (c in state.coins) {
            if (c.taken) continue
            drawBox(null, c.x, 1.1f, c.z, 1.3f, 1.3f, 1.3f, 0.96f, 0.78f, 0.24f, c.phase * 57.29578f)
        }

        for (t in state.traffic) drawCar(t.x, t.z, t.angle, t.cr, t.cg, t.cb)

        drawCar(state.car.x, state.car.z, state.car.angle, state.car.cr, state.car.cg, state.car.cb)
    }

    private fun drawCar(x: Float, z: Float, angle: Float, r: Float, g: Float, b: Float) {
        System.arraycopy(ident, 0, parent, 0, 16)
        Matrix.translateM(parent, 0, x, 0f, z)
        Matrix.rotateM(parent, 0, -angle * 57.29578f, 0f, 1f, 0f)

        drawBox(parent, 0f, 0.85f, 0f, 1.9f, 0.8f, 4.4f, r, g, b, 0f)
        drawBox(parent, 0f, 1.45f, -0.3f, 1.6f, 0.65f, 2.0f, r * 0.45f + 0.06f, g * 0.45f + 0.06f, b * 0.45f + 0.06f, 0f)
        drawBox(parent, 0f, 1.5f, 0.78f, 1.5f, 0.55f, 0.2f, 0.75f, 0.85f, 0.92f, 0f)
        drawBox(parent, -1.0f, 0.4f, 1.4f, 0.45f, 0.8f, 0.9f, 0.06f, 0.06f, 0.07f, 0f)
        drawBox(parent, 1.0f, 0.4f, 1.4f, 0.45f, 0.8f, 0.9f, 0.06f, 0.06f, 0.07f, 0f)
        drawBox(parent, -1.0f, 0.4f, -1.4f, 0.45f, 0.8f, 0.9f, 0.06f, 0.06f, 0.07f, 0f)
        drawBox(parent, 1.0f, 0.4f, -1.4f, 0.45f, 0.8f, 0.9f, 0.06f, 0.06f, 0.07f, 0f)
    }

    private fun drawBox(
        pm: FloatArray?,
        tx: Float, ty: Float, tz: Float,
        sx: Float, sy: Float, sz: Float,
        r: Float, g: Float, b: Float,
        rotY: Float
    ) {
        System.arraycopy(pm ?: ident, 0, model, 0, 16)
        Matrix.translateM(model, 0, tx, ty, tz)
        if (rotY != 0f) Matrix.rotateM(model, 0, rotY, 0f, 1f, 0f)
        Matrix.scaleM(model, 0, sx, sy, sz)

        Matrix.multiplyMM(tmp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, proj, 0, tmp, 0)

        GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(uModel, 1, false, model, 0)
        GLES20.glUniform3f(uColor, r, g, b)

        posBuf.position(0)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 0, posBuf)
        norBuf.position(0)
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, norBuf)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    private fun makeCube(): Pair<FloatBuffer, FloatBuffer> {
        val p = ArrayList<Float>()
        val n = ArrayList<Float>()

        fun face(v: FloatArray, nx: Float, ny: Float, nz: Float) {
            val order = intArrayOf(0, 1, 2, 0, 2, 3)
            for (idx in order) {
                p.add(v[idx * 3]); p.add(v[idx * 3 + 1]); p.add(v[idx * 3 + 2])
                n.add(nx); n.add(ny); n.add(nz)
            }
        }

        face(floatArrayOf(0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f), 1f, 0f, 0f)
        face(floatArrayOf(-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f), -1f, 0f, 0f)
        face(floatArrayOf(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f), 0f, 1f, 0f)
        face(floatArrayOf(-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f), 0f, -1f, 0f)
        face(floatArrayOf(-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f), 0f, 0f, 1f)
        face(floatArrayOf(0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f), 0f, 0f, -1f)

        return Pair(toBuffer(p), toBuffer(n))
    }

    private fun toBuffer(list: ArrayList<Float>): FloatBuffer {
        val arr = FloatArray(list.size)
        for (i in list.indices) arr[i] = list[i]
        val bb = ByteBuffer.allocateDirect(arr.size * 4)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(arr)
        fb.position(0)
        return fb
    }

    private fun buildProgram(vs: String, fs: String): Int {
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, v)
        GLES20.glAttachShader(prog, f)
        GLES20.glLinkProgram(prog)
        return prog
    }

    private fun compile(type: Int, src: String): Int {
        val sh = GLES20.glCreateShader(type)
        GLES20.glShaderSource(sh, src)
        GLES20.glCompileShader(sh)
        return sh
    }

    companion object {
        private const val VERT_SRC = """
            uniform mat4 uMVP;
            uniform mat4 uModel;
            attribute vec3 aPos;
            attribute vec3 aNormal;
            varying vec3 vNormal;
            void main() {
                gl_Position = uMVP * vec4(aPos, 1.0);
                vNormal = normalize((uModel * vec4(aNormal, 0.0)).xyz);
            }
        """

        private const val FRAG_SRC = """
            precision mediump float;
            uniform vec3 uColor;
            uniform vec3 uLightDir;
            varying vec3 vNormal;
            void main() {
                float d = max(dot(normalize(vNormal), normalize(uLightDir)), 0.0);
                float l = 0.35 + 0.65 * d;
                gl_FragColor = vec4(uColor * l, 1.0);
            }
        """
    }
}
