package com.luddosaurus.glasteroids
import android.graphics.Color
import android.opengl.GLES20

object Dot{
    val mesh = Mesh( floatArrayOf(0f, 0f, 0f), GLES20.GL_POINTS, norm = false)
}

class Star(x: Float, y: Float, color : Int = Color.MAGENTA) : GLEntity() {
    init {
        this.x = x
        this.y = y
        this.color[0] = Color.red(color) / 255f
        this.color[1] = Color.green(color) / 255f
        this.color[2] = Color.blue(color) / 255f
        this.color[3] = 0.5f
        this.mesh = Dot.mesh //all Stars use the exact same Mesh instance.
    }
}