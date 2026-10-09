package com.example.carsim

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object Ctrl {
    const val TOP = 0.58f
    const val SL_X1 = 0.18f
    const val SR_X1 = 0.36f
    const val BR_X0 = 0.62f
    const val GA_X0 = 0.78f
}

class Car {
    var x = 0f
    var z = 0f
    var angle = 0f
    var speed = 0f
    var steer = 0f
    var damage = 0f
    var fuel = 100f
    val cr = 0.85f
    val cg = 0.20f
    val cb = 0.20f
}

class Traffic {
    var x = 0f
    var z = 0f
    var angle = 0f
    var speed = 0f
    var cr = 0.2f
    var cg = 0.4f
    var cb = 0.9f
}

class Coin {
    var x = 0f
    var z = 0f
    var taken = false
    var phase = 0f
}

class Building {
    var x = 0f
    var z = 0f
    var w = 0f
    var d = 0f
    var h = 0f
    var cr = 0.5f
    var cg = 0.5f
    var cb = 0.55f
}

class Station {
    var x = 0f
    var z = 0f
}

class GameState {

    val car = Car()
    val buildings = ArrayList<Building>()
    val traffic = ArrayList<Traffic>()
    val coins = ArrayList<Coin>()
    val stations = ArrayList<Station>()

    var inLeft = false
    var inRight = false
    var inGas = false
    var inBrake = false

    var coinsCollected = 0
    var happyTimer = 0f
    var crashTimer = 0f
    var lowFuelWarn = false

    private val rnd = Random(7)

    companion object {
        const val ROAD_SPACING = 60f
        const val ROAD_HALF = 6f
        const val GRID = 7
        const val WORLD = ROAD_SPACING * (GRID - 1)
        const val MAX_SPEED = 38f
    }

    init {
        car.x = ROAD_SPACING
        car.z = ROAD_SPACING
        generate()
    }

    private fun generate() {
        val palette = arrayOf(
            floatArrayOf(0.48f, 0.50f, 0.56f),
            floatArrayOf(0.60f, 0.53f, 0.46f),
            floatArrayOf(0.41f, 0.46f, 0.52f),
            floatArrayOf(0.56f, 0.56f, 0.60f),
            floatArrayOf(0.49f, 0.45f, 0.39f)
        )
        for (i in 0 until GRID - 1) {
            for (j in 0 until GRID - 1) {
                val left = i * ROAD_SPACING + ROAD_HALF + 4f
                val right = (i + 1) * ROAD_SPACING - ROAD_HALF - 4f
                val top = j * ROAD_SPACING + ROAD_HALF + 4f
                val bottom = (j + 1) * ROAD_SPACING - ROAD_HALF - 4f
                val b = Building()
                b.x = (left + right) / 2f
                b.z = (top + bottom) / 2f
                b.w = right - left
                b.d = bottom - top
                b.h = 8f + rnd.nextFloat() * 34f
                val c = palette[rnd.nextInt(palette.size)]
                b.cr = c[0]
                b.cg = c[1]
                b.cb = c[2]
                buildings.add(b)
            }
        }

        var placed = 0
        while (placed < 45) {
            val onVert = rnd.nextBoolean()
            val line = rnd.nextInt(GRID)
            val pos = rnd.nextFloat() * WORLD
            val c = Coin()
            if (onVert) {
                c.x = line * ROAD_SPACING
                c.z = pos
            } else {
                c.x = pos
                c.z = line * ROAD_SPACING
            }
            if (hypot(c.x - car.x, c.z - car.z) > 30f) {
                coins.add(c)
                placed++
            }
        }

        var i = 0
        while (i < GRID) {
            var j = 0
            while (j < GRID) {
                val s = Station()
                s.x = i * ROAD_SPACING
                s.z = j * ROAD_SPACING
                stations.add(s)
                j += 3
            }
            i += 3
        }

        for (k in 0 until 12) {
            val t = Traffic()
            placeTraffic(t)
            traffic.add(t)
        }
    }

    private fun placeTraffic(t: Traffic) {
        val onVert = rnd.nextBoolean()
        val line = rnd.nextInt(GRID)
        if (onVert) {
            t.x = line * ROAD_SPACING
            t.z = rnd.nextFloat() * WORLD
            t.angle = if (rnd.nextBoolean()) (PI / 2).toFloat() else (-PI / 2).toFloat()
        } else {
            t.z = line * ROAD_SPACING
            t.x = rnd.nextFloat() * WORLD
            t.angle = if (rnd.nextBoolean()) 0f else PI.toFloat()
        }
        t.speed = 12f + rnd.nextFloat() * 12f
        val pal = arrayOf(
            floatArrayOf(0.24f, 0.36f, 0.80f),
            floatArrayOf(0.90f, 0.90f, 0.92f),
            floatArrayOf(0.16f, 0.16f, 0.19f),
            floatArrayOf(0.94f, 0.70f, 0.16f),
            floatArrayOf(0.35f, 0.66f, 0.35f)
        )
        val c = pal[rnd.nextInt(pal.size)]
        t.cr = c[0]
        t.cg = c[1]
        t.cb = c[2]
    }

    fun update(dt: Float) {
        car.steer = (if (inRight) 1f else 0f) - (if (inLeft) 1f else 0f)
        car.speed += (if (inGas && car.fuel > 0f) 26f * dt else 0f)
        if (inBrake) {
            val d = 60f * dt
            if (car.speed > 0f) car.speed = max(0f, car.speed - d) else car.speed = min(0f, car.speed + d)
        }
        val fr = 12f * dt
        if (car.speed > 0f) car.speed = max(0f, car.speed - fr)
        else if (car.speed < 0f) car.speed = min(0f, car.speed + fr)
        car.speed = car.speed.coerceIn(-12f, MAX_SPEED)

        car.angle += car.steer * 1.9f * dt * (car.speed / MAX_SPEED)

        car.x += cos(car.angle) * car.speed * dt
        car.z += sin(car.angle) * car.speed * dt
        car.x = car.x.coerceIn(0f, WORLD)
        car.z = car.z.coerceIn(0f, WORLD)

        if (inGas) car.fuel -= 0.6f * dt
        car.fuel = car.fuel.coerceIn(0f, 100f)

        if (happyTimer > 0f) happyTimer -= dt
        if (crashTimer > 0f) crashTimer -= dt

        for (b in buildings) collideBuilding(b)

        for (t in traffic) {
            t.x += cos(t.angle) * t.speed * dt
            t.z += sin(t.angle) * t.speed * dt
            if (t.x < -40f || t.x > WORLD + 40f || t.z < -40f || t.z > WORLD + 40f) placeTraffic(t)
            val dx = t.x - car.x
            val dz = t.z - car.z
            val d = hypot(dx, dz)
            if (d < 4.6f && d > 0.01f) {
                val ov = 4.6f - d
                car.x -= dx / d * ov * 0.5f
                car.z -= dz / d * ov * 0.5f
                t.x += dx / d * ov * 0.5f
                t.z += dz / d * ov * 0.5f
                car.speed *= -0.25f
                t.speed *= 0.4f
                car.damage = min(100f, car.damage + 12f)
                crashTimer = 1.2f
            }
        }

        for (c in coins) {
            if (c.taken) continue
            c.phase += dt * 3f
            if (hypot(c.x - car.x, c.z - car.z) < 3f) {
                c.taken = true
                coinsCollected++
                happyTimer = 1.4f
            }
        }

        for (s in stations) {
            if (hypot(s.x - car.x, s.z - car.z) < 7f && abs(car.speed) < 4f) {
                car.fuel = min(100f, car.fuel + 45f * dt)
                car.damage = max(0f, car.damage - 25f * dt)
            }
        }

        lowFuelWarn = car.fuel < 20f
    }

    private fun collideBuilding(b: Building) {
        val left = b.x - b.w / 2f
        val right = b.x + b.w / 2f
        val top = b.z - b.d / 2f
        val bottom = b.z + b.d / 2f
        val cx = car.x.coerceIn(left, right)
        val cz = car.z.coerceIn(top, bottom)
        val dx = car.x - cx
        val dz = car.z - cz
        val d2 = dx * dx + dz * dz
        val rr = 2.4f
        if (d2 < rr * rr) {
            if (d2 < 0.0001f) {
                val dl = car.x - left
                val dr = right - car.x
                val dtp = car.z - top
                val db = bottom - car.z
                val m = min(min(dl, dr), min(dtp, db))
                if (m == dl) car.x = left - rr
                else if (m == dr) car.x = right + rr
                else if (m == dtp) car.z = top - rr
                else car.z = bottom + rr
            } else {
                val d = sqrt(d2)
                val push = rr - d
                car.x += dx / d * push
                car.z += dz / d * push
            }
            car.speed *= -0.25f
            car.damage = min(100f, car.damage + 6f)
            crashTimer = 0.8f
        }
    }
}
