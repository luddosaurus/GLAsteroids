package com.luddosaurus.glasteroids
import android.os.SystemClock
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val TO_RADIANS = PI.toFloat() / 180.0f
private const val TAG = "Player"
class Player(x: Float, y: Float) : GLEntity() {
    init {
        this.x = x
        this.y = y
        this.mesh = Triangle.mesh
    }

    override fun render(viewportMatrix: FloatArray) {
        val uptime = SystemClock.uptimeMillis()
        val startPositionX = WORLD_WIDTH / 2f
        val startPositionY = WORLD_HEIGHT / 2f
        val rangeX = WORLD_WIDTH / 4f
        val rangeY = WORLD_HEIGHT / 4f
        val speed = 360f / 2000f
        var angle = (uptime * speed) % 360f
        val fiveSeconds = uptime % 5000

        x = startPositionX + (cos(angle * TO_RADIANS) * rangeX)
        y = startPositionY + (sin(angle * TO_RADIANS) * rangeY)
        rotation = (360.0f / 5000.0f) * fiveSeconds
        scale = 5f

        //ask the super class (GLEntity) to render us
        super.render(viewportMatrix)
    }

}