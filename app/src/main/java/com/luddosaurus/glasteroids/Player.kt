package com.luddosaurus.glasteroids
import android.os.SystemClock
import android.util.Log
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val TO_RADIANS = PI.toFloat() / 180.0f
const val ROTATION_VELOCITY = 360f //TODO: game play values!
const val THRUST = 4f
const val DRAG = 0.99f

private const val TAG = "Player"
class Player(x: Float, y: Float) : GLEntity() {
    init {
        this.x = x
        this.y = y
        width = 1f; //TO DO: gameplay values! move to configs
        height = 1.5f;
        scale = 5f

        this.mesh = Triangle.mesh

        mesh.setWidthHeight(width, height);
        mesh.flipY();
    }

    override fun render(viewportMatrix: FloatArray) {

        //ask the super class (GLEntity) to render us
        super.render(viewportMatrix)
    }

    override fun update(dt: Float) {
        rotation += dt * ROTATION_VELOCITY * engine.inputs.horizontalFactor
        if (engine.inputs.pressingB || engine.inputs.pressingA) {
            val thrust = if (engine.inputs.pressingB) THRUST else -THRUST
            val theta = rotation * TO_RADIANS
            velX += sin(theta) * thrust
            velY -= cos(theta) * thrust
        }
        velX *= DRAG
        velY *= DRAG
        super.update(dt)
    }
}