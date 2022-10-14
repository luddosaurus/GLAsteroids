package com.luddosaurus.glasteroids

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin

private val BULLET_MESH = Dot.mesh
const val SPEED = 120f
const val BULLET_TIME_TO_LIVE = 0.5f //seconds

class Bullet : TTLEntity() {
    init {
        ttl = BULLET_TIME_TO_LIVE
        setColors(1f, 0f, 1f, 1f)
        mesh = BULLET_MESH
    }

    override fun fireFrom(source: GLEntity) {
        engine.onGameEvent(GameEvent.PEW, this)
        val theta = source.rotation * TO_RADIANS
        x = source.x + sin(theta) * (source.width * 0.5f)
        y = source.y - cos(theta) * (source.height * 0.5f)
        velX = source.velX
        velY = source.velY
        velX += sin(theta) * SPEED
        velY -= cos(theta) * SPEED
        ttl = BULLET_TIME_TO_LIVE
    }


    override fun isColliding(that: GLEntity): Boolean {
        if (!areBoundingSpheresOverlapping(this, that)) { //quick rejection
            return false
        }
        val asteroidVertices: ArrayList<PointF> = that.getPointList()
        return polygonVsPoint(asteroidVertices, x, y)
    }

    override fun onCollision(that: GLEntity?) {
        if (that is Asteroid) ttl = 0f
        super.onCollision(that)
    }
}