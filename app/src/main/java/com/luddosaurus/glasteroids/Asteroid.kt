package com.luddosaurus.glasteroids
import android.opengl.GLES20
import kotlin.random.Random

private const val MAX_VEL = 8f
private const val MIN_VEL = -8f
private const val BASE_SIZE = 4f
private const val BASE_VEL = 8f


fun between(min: Float, max: Float): Float = min + Random.nextFloat() * (max - min)

enum class AsteroidSize(
    val size: Float,
    val velocity: Float,
    val points: Int,
    val splitNbr: Int,
    val childType: AsteroidSize?
) {
    SMALL(BASE_SIZE, BASE_VEL*3, 3, 0, null),
    MEDIUM(BASE_SIZE*2, BASE_VEL*2, 2, 3, AsteroidSize.SMALL),
    LARGE(BASE_SIZE*3, BASE_VEL*1,1,2, AsteroidSize.MEDIUM)
}

class Asteroid(x: Float, y: Float, val type : AsteroidSize) : GLEntity(){

    init{
        this.x = x
        this.y = y
        width = type.size
        height = width;
        velX = between(-type.velocity, type.velocity)
        velY = between(-type.velocity, type.velocity)

        val points = Random.nextInt(6) + 3
        val radius = width * 0.5f
        this.mesh = Mesh(
            generateLinePolygon(points, radius),
            GLES20.GL_LINES,
        )
        mesh.setWidthHeight(width, height);
    }

    override fun onCollision(that: GLEntity?) {
        if (that is Bullet)
            engine.onGameEvent(GameEvent.HIT, this)

        super.onCollision(that)

    }
}