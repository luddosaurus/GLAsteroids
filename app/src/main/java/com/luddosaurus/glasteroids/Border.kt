package com.luddosaurus.glasteroids

import android.opengl.GLES20

class Border(
    x: Float,
    y: Float,
    worldWidth: Float,
    worldHeight: Float,
) : GLEntity() {
    init {
        this.x = x
        this.y = y
        setColors(1f, 0f, 0f, 1f) //RED for visibility
        val borderVertices = floatArrayOf(
            // A line from point 1 to point 2
            0f, 0f, 0f,
            worldWidth, 0f, 0f,
            // Point 2 to point 3
            worldWidth, 0f, 0f,
            worldWidth, worldHeight, 0f,
            // Point 3 to point 4
            worldWidth, worldHeight, 0f,
            0f, worldHeight, 0f,
            // Point 4 to point 1
            0f, worldHeight,
            0f, 0f, 0f, 0f
        )
        mesh = Mesh(borderVertices, GLES20.GL_LINES)
    }
}