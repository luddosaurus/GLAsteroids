package com.luddosaurus.glasteroids

import android.opengl.Matrix


private const val WORLD_WIDTH = 50f//all dimensions are in meters
private const val WORLD_HEIGHT = 50f
//private const val METERS_TO_SHOW_X = 144f//160m x 90m, the entire game world in view
//private const val METERS_TO_SHOW_Y = 70f //TO DO: calculate to match screen aspect ratio

class Camera(screenWidth: Int, screenHeight: Int) {

    private val viewportMatrix = FloatArray(4 * 4) // Camera
    var worldWidth = WORLD_WIDTH
    var worldHeight = WORLD_HEIGHT

    var frameWidth = 0f
    var frameHeight = 0f
    init {
        val ratio = screenWidth.toFloat()/screenHeight.toFloat()
        worldWidth = worldHeight * ratio
        frameWidth = worldWidth
        frameHeight = worldHeight

        // todo calculate aspect ratio
    }

    fun worldCenterX() = (worldWidth / 2)
    fun worldCenterY() = worldHeight / 2

    fun lookAt(x : Float, y : Float) {
        // todo
    }

    fun getViewportMatrix(): FloatArray {
        val offset = 0
        val left = 0f
        val right = frameWidth
        val bottom = frameHeight
        val top = 0f
        val near = 0f
        val far = 1f
        Matrix.orthoM(viewportMatrix, offset, left, right, bottom, top, near, far)
        return viewportMatrix
    }


}