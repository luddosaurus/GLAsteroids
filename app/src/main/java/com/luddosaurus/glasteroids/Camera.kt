package com.luddosaurus.glasteroids

import android.opengl.Matrix


const val WORLD_WIDTH = 144f//all dimensions are in meters
const val WORLD_HEIGHT = 70f
const val METERS_TO_SHOW_X = 144f//160m x 90m, the entire game world in view
const val METERS_TO_SHOW_Y = 70f //TO DO: calculate to match screen aspect ratio

class Camera(screenWidth: Int, screenHeight: Int) {

    private val viewportMatrix = FloatArray(4 * 4) // Camera

    init {
        // todo calculate aspect ratio
    }

    fun lookAt(x : Float, y : Float) {
        // todo
    }

    fun getViewportMatrix(): FloatArray {
        val offset = 0
        val left = 0f
        val right = METERS_TO_SHOW_X
        val bottom = METERS_TO_SHOW_Y
        val top = 0f
        val near = 0f
        val far = 1f
        Matrix.orthoM(viewportMatrix, offset, left, right, bottom, top, near, far)
        return viewportMatrix
    }


}