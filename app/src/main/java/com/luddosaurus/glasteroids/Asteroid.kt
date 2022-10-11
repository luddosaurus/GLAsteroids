package com.luddosaurus.glasteroids
import android.opengl.GLES20
import kotlin.random.Random

private const val MAX_VEL = 8f
private const val MIN_VEL = -8f

fun between(min: Float, max: Float): Float = min + Random.nextFloat() * (max - min)

class Asteroid(x: Float, y: Float, points: Int) : GLEntity(){
    init{
        assert(points >= 3) { "triangles or more, please. :)" }
        this.x = x
        this.y = y
        width = 12f; //TO DO: gameplay settings
        height = width;
        velX = between(MIN_VEL, MAX_VEL)
        velY = between(MIN_VEL, MAX_VEL)

        val radius = width*0.5f
        this.mesh = Mesh(
            generateLinePolygon(points, radius),
            GLES20.GL_LINES,
        )
        mesh.setWidthHeight(width, height);
    }

}