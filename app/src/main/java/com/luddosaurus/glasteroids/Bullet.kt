package com.luddosaurus.glasteroids

import kotlin.math.cos
import kotlin.math.sin

private val BULLET_MESH = Dot.mesh //reusing the Dot (defined in Star.kt, but available throughout the package)
const val SPEED = 120f //TO DO: game play settings
const val TIME_TO_LIVE = 3.0f //seconds

class Bullet : GLEntity() {
    var ttl = TIME_TO_LIVE
    init {
        setColors(1f, 0f, 1f, 1f)
        mesh = BULLET_MESH //all bullets use the exact same mesh
        scale = 2f
    }

    fun fireFrom(source: GLEntity) {
        val theta = source.rotation * TO_RADIANS
        x = source.x + sin(theta) * (source.width * 0.5f)
        y = source.y - cos(theta) * (source.height * 0.5f)
        velX = source.velX
        velY = source.velY
        velX += sin(theta) * SPEED
        velY -= cos(theta) * SPEED
        ttl = TIME_TO_LIVE
    }

    val isAlive: Boolean
        get() = ttl > 0

    override fun update(dt: Float) {
        if (ttl > 0) {
            ttl -= dt
            super.update(dt)
        }
    }

    override fun render(viewportMatrix: FloatArray) {
        if (ttl > 0) {
            super.render(viewportMatrix)
        }
    }
}