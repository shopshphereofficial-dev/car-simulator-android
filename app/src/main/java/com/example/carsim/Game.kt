package com.example.carsim

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val ROAD_SPACING = 950f
private const val ROAD_HALF = 150f
private const val GRID = 7
private const val WORLD = ROAD_SPACING * (GRID - 1)

object Ctrl {
    const val TOP = 0.56f
    const val SL_X1 = 0.18f
    const val SR_X1 = 0.36f
    const val BR_X0 = 0.62f
    const val GA_X0 = 0.78f
}

class Car {
    var x = ROAD_SPACING
    var y = ROAD_SPACING
    var angle = 0f
    var speed = 0f
    var steer = 0f
    var throttle = 0f
    var braking = false
    var damage = 0f
    var fuel = 100f
    var color = Color.rgb(226, 66, 66)
}

class Traffic {
    var x = 0f
    var y = 0f
    var angle = 0f
    var speed = 200f
    var color = 0
}

class Coin {
    var x = 0f
    var y = 0f
    var taken = false
    var phase = 0f
}

class Building {
    var rect = RectF()
    var color = 0
    var park = false
}

class FuelStation {
    var x = 0f
    var y = 0f
}

class Game {

    val car = Car()
    val buildings = ArrayList<Building>()
    val traffic = ArrayList<Traffic>()
    val coins = ArrayList<Coin>()
    val stations = ArrayList<FuelStation>()

    var inLeft = false
    var inRight = false
    var inGas = false
    var inBrake = false

    var coinsCollected = 0
    var happyTimer = 0f
    var crashTimer = 0f
    var lowFuelWarn = false
    var time = 0f

    private val rnd = Random(7)

    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val winPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val treePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stationPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pumpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coinPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val carPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapBg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapRoad = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapCoin = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mapPlayer = Paint(Paint.ANTI_ALIAS_FLAG)
    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        shadowPaint.color = Color.argb(90, 0, 0, 0)
        dashPaint.color = Color.rgb(232, 210, 90)
        dashPaint.strokeWidth = 8f
        dashPaint.style = Paint.Style.STROKE
        winPaint.color = Color.argb(150, 240, 240, 200)
        treePaint.color = Color.rgb(46, 110, 46)
        pumpPaint.color = Color.WHITE
        mapRoad.color = Color.argb(120, 130, 140, 150)
        mapRoad.strokeWidth = 4f
        mapCoin.color = Color.rgb(245, 200, 60)
        mapPlayer.color = Color.rgb(235, 70, 70)
        generate()
    }

    private fun generate() {
        for (i in 0 until GRID - 1) {
            for (j in 0 until GRID - 1) {
                val left = i * ROAD_SPACING + ROAD_HALF + 50f
                val top = j * ROAD_SPACING + ROAD_HALF + 50f
                val right = (i + 1) * ROAD_SPACING - ROAD_HALF - 50f
                val bottom = (j + 1) * ROAD_SPACING - ROAD_HALF - 50f
                val b = Building()
                b.rect = RectF(left, top, right, bottom)
                if (rnd.nextFloat() < 0.18f) {
                    b.park = true
                    b.color = Color.rgb(74, 140, 74)
                } else {
                    b.color = buildingColor()
                }
                buildings.add(b)
            }
        }

        var placed = 0
        while (placed < 50) {
            val onVert = rnd.nextBoolean()
            val line = rnd.nextInt(GRID)
            val pos = rnd.nextFloat() * WORLD
            val c = Coin()
            if (onVert) {
                c.x = line * ROAD_SPACING
                c.y = pos
            } else {
                c.x = pos
                c.y = line * ROAD_SPACING
            }
            if (hypot(c.x - car.x, c.y - car.y) > 320f) {
                coins.add(c)
                placed++
            }
        }

        var i = 0
        while (i < GRID) {
            var j = 0
            while (j < GRID) {
                val s = FuelStation()
                s.x = i * ROAD_SPACING
                s.y = j * ROAD_SPACING
                stations.add(s)
                j += 3
            }
            i += 3
        }

        for (k in 0 until 16) spawnTraffic()
    }

    private fun buildingColor(): Int {
        val palette = intArrayOf(
            Color.rgb(122, 128, 142),
            Color.rgb(150, 132, 118),
            Color.rgb(104, 118, 132),
            Color.rgb(140, 140, 150),
            Color.rgb(126, 116, 100)
        )
        return palette[rnd.nextInt(palette.size)]
    }

    private fun trafficColor(): Int {
        val palette = intArrayOf(
            Color.rgb(60, 90, 200),
            Color.rgb(230, 230, 235),
            Color.rgb(40, 40, 48),
            Color.rgb(240, 180, 40),
            Color.rgb(90, 170, 90)
        )
        return palette[rnd.nextInt(palette.size)]
    }

    private fun spawnTraffic() {
        val t = Traffic()
        val onVert = rnd.nextBoolean()
        val line = rnd.nextInt(GRID)
        if (onVert) {
            t.x = line * ROAD_SPACING
            t.y = rnd.nextFloat() * WORLD
            t.angle = if (rnd.nextBoolean()) (PI / 2).toFloat() else (-PI / 2).toFloat()
        } else {
            t.y = line * ROAD_SPACING
            t.x = rnd.nextFloat() * WORLD
            t.angle = if (rnd.nextBoolean()) 0f else PI.toFloat()
        }
        t.speed = 160f + rnd.nextFloat() * 140f
        t.color = trafficColor()
        traffic.add(t)
    }

    private fun respawn(t: Traffic) {
        val onVert = rnd.nextBoolean()
        val line = rnd.nextInt(GRID)
        if (onVert) {
            t.x = line * ROAD_SPACING
            t.y = rnd.nextFloat() * WORLD
            t.angle = if (rnd.nextBoolean()) (PI / 2).toFloat() else (-PI / 2).toFloat()
        } else {
            t.y = line * ROAD_SPACING
            t.x = rnd.nextFloat() * WORLD
            t.angle = if (rnd.nextBoolean()) 0f else PI.toFloat()
        }
        t.speed = 160f + rnd.nextFloat() * 140f
    }

    fun update(dt: Float) {
        time += dt

        car.steer = (if (inRight) 1f else 0f) - (if (inLeft) 1f else 0f)
        car.throttle = if (inGas) 1f else 0f
        car.braking = inBrake

        val accel = 640f
        val maxSpeed = 700f
        val maxReverse = -230f

        if (car.throttle > 0f && car.fuel > 0f) car.speed += accel * car.throttle * dt
        if (car.braking) {
            val d = 1500f * dt
            if (car.speed > 0f) car.speed = max(0f, car.speed - d)
            else car.speed = min(0f, car.speed + d)
        }

        val fr = 300f * dt
        if (car.speed > 0f) car.speed = max(0f, car.speed - fr)
        else if (car.speed < 0f) car.speed = min(0f, car.speed + fr)
        car.speed = car.speed.coerceIn(maxReverse, maxSpeed)

        car.angle += car.steer * 2.5f * dt * (car.speed / maxSpeed)

        car.x += cos(car.angle) * car.speed * dt
        car.y += sin(car.angle) * car.speed * dt
        car.x = car.x.coerceIn(0f, WORLD)
        car.y = car.y.coerceIn(0f, WORLD)

        if (car.throttle > 0f) car.fuel -= 0.55f * dt
        car.fuel = car.fuel.coerceIn(0f, 100f)

        if (happyTimer > 0f) happyTimer -= dt
        if (crashTimer > 0f) crashTimer -= dt

        val carR = 46f
        for (b in buildings) {
            if (b.park) continue
            collideRect(b.rect, carR)
        }

        for (t in traffic) {
            t.x += cos(t.angle) * t.speed * dt
            t.y += sin(t.angle) * t.speed * dt
            if (t.x < -250f || t.x > WORLD + 250f || t.y < -250f || t.y > WORLD + 250f) {
                respawn(t)
            }
            val dx = t.x - car.x
            val dy = t.y - car.y
            val d = hypot(dx, dy)
            if (d < 82f && d > 0.01f) {
                val overlap = 82f - d
                car.x -= dx / d * overlap * 0.5f
                car.y -= dy / d * overlap * 0.5f
                t.x += dx / d * overlap * 0.5f
                t.y += dy / d * overlap * 0.5f
                car.speed *= -0.25f
                t.speed *= 0.3f
                car.damage = min(100f, car.damage + 12f)
                crashTimer = 1.2f
            }
        }

        for (c in coins) {
            if (c.taken) continue
            c.phase += dt * 3f
            if (hypot(c.x - car.x, c.y - car.y) < 46f) {
                c.taken = true
                coinsCollected++
                happyTimer = 1.4f
            }
        }

        for (s in stations) {
            if (hypot(s.x - car.x, s.y - car.y) < 130f && abs(car.speed) < 70f) {
                car.fuel = min(100f, car.fuel + 45f * dt)
                car.damage = max(0f, car.damage - 25f * dt)
            }
        }

        lowFuelWarn = car.fuel < 20f
    }

    private fun collideRect(r: RectF, carR: Float) {
        val cx = car.x.coerceIn(r.left, r.right)
        val cy = car.y.coerceIn(r.top, r.bottom)
        val dx = car.x - cx
        val dy = car.y - cy
        val d2 = dx * dx + dy * dy
        if (d2 < carR * carR) {
            if (d2 < 1f) {
                val dl = car.x - r.left
                val dr = r.right - car.x
                val dtp = car.y - r.top
                val dbt = r.bottom - car.y
                val m = min(min(dl, dr), min(dtp, dbt))
                if (m == dl) car.x = r.left - carR
                else if (m == dr) car.x = r.right + carR
                else if (m == dtp) car.y = r.top - carR
                else car.y = r.bottom + carR
            } else {
                val d = sqrt(d2)
                val push = carR - d
                car.x += dx / d * push
                car.y += dy / d * push
            }
            car.speed *= -0.25f
            car.damage = min(100f, car.damage + 6f)
            crashTimer = 0.8f
        }
    }

    fun draw(canvas: Canvas, w: Float, h: Float) {
        val ox = w / 2f - car.x
        val oy = h / 2f - car.y

        canvas.drawColor(Color.rgb(56, 100, 56))

        roadPaint.color = Color.rgb(72, 74, 80)
        var i = 0
        while (i < GRID) {
            val yy = i * ROAD_SPACING + oy
            canvas.drawRect(0f, yy - ROAD_HALF, w, yy + ROAD_HALF, roadPaint)
            i++
        }
        i = 0
        while (i < GRID) {
            val xx = i * ROAD_SPACING + ox
            canvas.drawRect(xx - ROAD_HALF, 0f, xx + ROAD_HALF, h, roadPaint)
            i++
        }

        i = 0
        while (i < GRID) {
            val yy = i * ROAD_SPACING + oy
            var x = (ox % 130f) - 130f
            while (x < w) {
                canvas.drawLine(x, yy, x + 62f, yy, dashPaint)
                x += 130f
            }
            i++
        }
        i = 0
        while (i < GRID) {
            val xx = i * ROAD_SPACING + ox
            var y = (oy % 130f) - 130f
            while (y < h) {
                canvas.drawLine(xx, y, xx, y + 62f, dashPaint)
                y += 130f
            }
            i++
        }

        for (s in stations) {
            val sx = s.x + ox
            val sy = s.y + oy
            if (sx < -160f || sx > w + 160f || sy < -160f || sy > h + 160f) continue
            stationPaint.color = Color.rgb(40, 150, 90)
            canvas.drawRoundRect(RectF(sx - 120f, sy - 120f, sx + 120f, sy + 120f), 18f, 18f, stationPaint)
            pumpPaint.color = Color.WHITE
            canvas.drawRoundRect(RectF(sx - 22f, sy - 46f, sx + 22f, sy + 46f), 8f, 8f, pumpPaint)
            pumpPaint.color = Color.rgb(40, 150, 90)
            canvas.drawRect(sx - 10f, sy - 30f, sx + 10f, sy + 6f, pumpPaint)
        }

        for (c in coins) {
            if (c.taken) continue
            val sx = c.x + ox
            val sy = c.y + oy
            if (sx < -60f || sx > w + 60f || sy < -60f || sy > h + 60f) continue
            val sc = 0.35f + 0.65f * abs(cos(c.phase))
            coinPaint.style = Paint.Style.FILL
            coinPaint.color = Color.rgb(245, 200, 60)
            canvas.drawOval(RectF(sx - 22f * sc, sy - 22f, sx + 22f * sc, sy + 22f), coinPaint)
            coinPaint.color = Color.rgb(200, 150, 30)
            coinPaint.style = Paint.Style.STROKE
            coinPaint.strokeWidth = 5f
            canvas.drawOval(RectF(sx - 22f * sc, sy - 22f, sx + 22f * sc, sy + 22f), coinPaint)
            coinPaint.style = Paint.Style.FILL
        }

        for (b in buildings) {
            val l = b.rect.left + ox
            val t = b.rect.top + oy
            val r = b.rect.right + ox
            val bo = b.rect.bottom + oy
            if (r < -60f || l > w + 60f || bo < -60f || t > h + 60f) continue
            canvas.drawRoundRect(RectF(l + 10f, t + 12f, r + 10f, bo + 12f), 16f, 16f, shadowPaint)
            bPaint.color = b.color
            canvas.drawRoundRect(RectF(l, t, r, bo), 16f, 16f, bPaint)
            if (b.park) {
                val cxp = (l + r) / 2f
                val cyp = (t + bo) / 2f
                canvas.drawCircle(cxp - 70f, cyp, 44f, treePaint)
                canvas.drawCircle(cxp + 60f, cyp + 40f, 52f, treePaint)
                canvas.drawCircle(cxp + 10f, cyp - 70f, 40f, treePaint)
            } else {
                var wx = l + 44f
                while (wx < r - 70f) {
                    var wy = t + 44f
                    while (wy < bo - 70f) {
                        canvas.drawRect(wx, wy, wx + 34f, wy + 34f, winPaint)
                        wy += 72f
                    }
                    wx += 72f
                }
            }
        }

        for (t in traffic) drawCar(canvas, t.x, t.y, t.angle, t.color, ox, oy)

        drawCar(canvas, car.x, car.y, car.angle, car.color, ox, oy)
        drawBrakeLights(canvas, car.x, car.y, car.angle, ox, oy)

        drawHud(canvas, w, h)
    }

    private fun drawCar(canvas: Canvas, wx: Float, wy: Float, angle: Float, color: Int, ox: Float, oy: Float) {
        val sx = wx + ox
        val sy = wy + oy
        canvas.save()
        canvas.translate(sx, sy)
        canvas.rotate(angle * 57.29578f)
        val len = 96f
        val wid = 48f

        carPaint.color = Color.argb(80, 0, 0, 0)
        canvas.drawRoundRect(RectF(-len / 2f + 6f, -wid / 2f + 8f, len / 2f + 6f, wid / 2f + 8f), 14f, 14f, carPaint)

        wheelPaint.color = Color.rgb(24, 24, 24)
        canvas.drawRoundRect(RectF(-len * 0.36f, -wid / 2f - 7f, -len * 0.16f, -wid / 2f + 7f), 6f, 6f, wheelPaint)
        canvas.drawRoundRect(RectF(-len * 0.36f, wid / 2f - 7f, -len * 0.16f, wid / 2f + 7f), 6f, 6f, wheelPaint)
        canvas.drawRoundRect(RectF(len * 0.16f, -wid / 2f - 7f, len * 0.36f, -wid / 2f + 7f), 6f, 6f, wheelPaint)
        canvas.drawRoundRect(RectF(len * 0.16f, wid / 2f - 7f, len * 0.36f, wid / 2f + 7f), 6f, 6f, wheelPaint)

        carPaint.color = color
        canvas.drawRoundRect(RectF(-len / 2f, -wid / 2f, len / 2f, wid / 2f), 16f, 16f, carPaint)

        glassPaint.color = Color.argb(200, 150, 200, 230)
        canvas.drawRoundRect(RectF(len * 0.20f, -wid * 0.30f, len * 0.40f, wid * 0.30f), 7f, 7f, glassPaint)

        carPaint.color = Color.argb(150, 24, 24, 34)
        canvas.drawRoundRect(RectF(-len * 0.22f, -wid * 0.34f, len * 0.18f, wid * 0.34f), 9f, 9f, carPaint)

        glassPaint.color = Color.argb(180, 140, 190, 220)
        canvas.drawRoundRect(RectF(-len * 0.40f, -wid * 0.28f, -len * 0.24f, wid * 0.28f), 6f, 6f, glassPaint)

        canvas.restore()
    }

    private fun drawBrakeLights(canvas: Canvas, wx: Float, wy: Float, angle: Float, ox: Float, oy: Float) {
        if (!car.braking) return
        canvas.save()
        canvas.translate(wx + ox, wy + oy)
        canvas.rotate(angle * 57.29578f)
        carPaint.color = Color.rgb(255, 60, 40)
        canvas.drawRoundRect(RectF(-50f, -20f, -42f, -8f), 4f, 4f, carPaint)
        canvas.drawRoundRect(RectF(-50f, 8f, -42f, 20f), 4f, 4f, carPaint)
        canvas.restore()
    }

    private fun drawHud(canvas: Canvas, w: Float, h: Float) {
        val kmh = abs(car.speed) * 0.32f

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 66f
        textPaint.color = Color.WHITE
        val num = kmh.toInt().toString()
        canvas.drawText(num, 44f, 100f, textPaint)
        textPaint.textSize = 26f
        textPaint.color = Color.rgb(210, 220, 230)
        canvas.drawText("km/h", 44f + textPaint.measureText(num) + 60f, 100f, textPaint)

        drawBar(canvas, 44f, 124f, 320f, 30f, car.fuel / 100f, Color.rgb(60, 200, 120), "FUEL")
        drawBar(canvas, 44f, 166f, 320f, 30f, car.damage / 100f, Color.rgb(230, 90, 80), "DAMAGE")

        coinPaint.style = Paint.Style.FILL
        coinPaint.color = Color.rgb(245, 200, 60)
        canvas.drawCircle(w - 360f, 70f, 24f, coinPaint)
        coinPaint.color = Color.rgb(200, 150, 30)
        coinPaint.style = Paint.Style.STROKE
        coinPaint.strokeWidth = 4f
        canvas.drawCircle(w - 360f, 70f, 24f, coinPaint)
        coinPaint.style = Paint.Style.FILL
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 46f
        textPaint.color = Color.WHITE
        canvas.drawText(coinsCollected.toString(), w - 320f, 86f, textPaint)

        drawMinimap(canvas, w)

        drawDriver(canvas, w)

        val top = h * Ctrl.TOP
        drawBtn(canvas, 0f, top, w * Ctrl.SL_X1, h, "<", inLeft)
        drawBtn(canvas, w * Ctrl.SR_X1 - w * Ctrl.SL_X1 + w * 0f, top, w * Ctrl.SR_X1, h, ">", inRight)
        drawBtn(canvas, w * Ctrl.BR_X0, top, w * Ctrl.BR_X0 + (w * Ctrl.GA_X0 - w * Ctrl.BR_X0), h, "BRAKE", inBrake)
        drawBtn(canvas, w * Ctrl.GA_X0, top, w, h, "GAS", inGas)
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
        val mSize = 180f
        val mx = w - mSize - 26f
        val my = 26f
        mapBg.color = Color.argb(150, 20, 24, 30)
        canvas.drawRoundRect(RectF(mx, my, mx + mSize, my + mSize), 16f, 16f, mapBg)
        val sc = mSize / WORLD
        var i = 0
        while (i < GRID) {
            val yy = my + i * ROAD_SPACING * sc
            canvas.drawLine(mx, yy, mx + mSize, yy, mapRoad)
            i++
        }
        i = 0
        while (i < GRID) {
            val xx = mx + i * ROAD_SPACING * sc
            canvas.drawLine(xx, my, xx, my + mSize, mapRoad)
            i++
        }
        for (c in coins) {
            if (c.taken) continue
            canvas.drawCircle(mx + c.x * sc, my + c.y * sc, 3f, mapCoin)
        }
        canvas.drawCircle(mx + car.x * sc, my + car.y * sc, 7f, mapPlayer)
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

        if (crashTimer > 0f) {
            eyePaint.color = Color.rgb(30, 30, 30)
            eyePaint.style = Paint.Style.STROKE
            eyePaint.strokeWidth = 6f
            drawX(canvas, cx - 20f, cy - 6f, 11f)
            drawX(canvas, cx + 20f, cy - 6f, 11f)
            eyePaint.style = Paint.Style.FILL
            mouthPaint.color = Color.rgb(120, 30, 30)
            canvas.drawOval(RectF(cx - 15f, cy + 16f, cx + 15f, cy + 42f), mouthPaint)
            drawBubble(canvas, cx + r + 20f, cy - 40f, "Ouch!")
        } else if (happyTimer > 0f) {
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
            if (lowFuelWarn) {
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
