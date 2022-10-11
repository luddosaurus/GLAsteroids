package com.luddosaurus.glasteroids
import android.graphics.Color
import android.opengl.GLES20

object Dot{
    val mesh = Mesh( floatArrayOf(0f, 0f, 0f), GLES20.GL_POINTS, norm = false)
}

class Star(x: Float, y: Float) : GLEntity() {
    init {
        this.x = x
        this.y = y
        this.color[0] = Color.red(Color.MAGENTA) / 255f
        this.color[1] = Color.green(Color.MAGENTA) / 255f
        this.color[2] = Color.blue(Color.MAGENTA) / 255f
        this.color[3] = 1f
        this.mesh = Dot.mesh //all Stars use the exact same Mesh instance.
    }
}