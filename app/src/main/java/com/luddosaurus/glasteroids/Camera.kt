package com.luddosaurus.glasteroids

import android.opengl.Matrix


private const val WORLD_WIDTH = 100f//all dimensions are in meters
private const val WORLD_HEIGHT = 100f
private const val METERS_TO_SHOW_X = 100f//160m x 90m, the entire game world in view
private const val METERS_TO_SHOW_Y = 70f //TO DO: calculate to match screen aspect ratio

class Camera(screenWidth: Int, screenHeight: Int) {

    private val viewportMatrix = FloatArray(4 * 4) // Camera
    var worldWidth = 0f
    var worldHeight = 0f

    var frameWidth = 0f
    var frameHeight = 0f

    var x = worldCenterX()
    var y = worldCenterY()
    init {
        val ratio = screenWidth.toFloat()/screenHeight.toFloat()
        worldHeight = WORLD_HEIGHT
        worldWidth = WORLD_HEIGHT * ratio
//        frameWidth = worldWidth
//        frameHeight = worldHeight
        frameHeight = METERS_TO_SHOW_Y
        frameWidth = METERS_TO_SHOW_Y * ratio

    }

    fun worldCenterX() = (worldWidth / 2)
    fun worldCenterY() = worldHeight / 2

    fun lookAt(x : Float, y : Float) {
        this.x = x
        this.y = y
    }

    fun getViewportMatrix(): FloatArray {
        val offset = 0
        val left = x - frameWidth / 2
        val right = x + frameWidth / 2
        val bottom = y + frameHeight / 2
        val top = y - frameHeight / 2
        val near = 0f
        val far = 1f
        Matrix.orthoM(viewportMatrix, offset, left, right, bottom, top, near, far)
        return viewportMatrix
    }

//    fun getViewportMatrix(): FloatArray {
//        val offset = 0
//        val left = 0f
//        val right = frameWidth
//        val bottom = frameHeight
//        val top = 0f
//        val near = 0f
//        val far = 1f
//        Matrix.orthoM(viewportMatrix, offset, left, right, bottom, top, near, far)
//        return viewportMatrix
//    }


}